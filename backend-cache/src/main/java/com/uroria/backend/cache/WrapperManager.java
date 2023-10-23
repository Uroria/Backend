package com.uroria.backend.cache;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.uroria.backend.Backend;
import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.broadcast.BroadcastListener;
import com.uroria.backend.communication.broadcast.BroadcastPoint;
import com.uroria.backend.communication.broadcast.Broadcaster;
import com.uroria.backend.communication.request.RequestPoint;
import com.uroria.backend.communication.request.Requester;
import com.uroria.base.event.EventManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.slf4j.Logger;

public abstract class WrapperManager<T extends Wrapper> {
    protected final Logger logger;
    protected final RequestPoint requestPoint;
    protected final BroadcastPoint broadcastPoint;
    protected final Requester<PartRequest, PartResponse> partRequester;
    protected final Broadcaster<UpdateBroadcast> updateBroadcaster;
    protected final Broadcaster<DeleteBroadcast> deleteBroadcaster;
    protected final ObjectSet<T> wrappers;
    protected final EventManager eventManager;

    public WrapperManager(Logger logger, RequestPoint requestPoint, BroadcastPoint broadcastPoint, String topic) {
        this.logger = logger;
        this.requestPoint = requestPoint;
        this.broadcastPoint = broadcastPoint;
        this.wrappers = new ObjectArraySet<>();
        this.partRequester = requestPoint.registerRequester(PartRequest.class, PartResponse.class, topic + "-part");
        this.updateBroadcaster = broadcastPoint.registerBroadcaster(UpdateBroadcast.class, topic + "-part");
        this.deleteBroadcaster = broadcastPoint.registerBroadcaster(DeleteBroadcast.class, topic + "-delete");
        this.eventManager = Backend.getEventManager();
    }

    public WrapperManager(Logger logger, Communicator communicator, String requestPointTopic, String broadcastPointTopic, String topic) {
        this(logger, new RequestPoint(communicator, requestPointTopic), new BroadcastPoint(communicator, broadcastPointTopic), topic);
    }

    public void enable() {
        this.updateBroadcaster.registerListener(new BroadcastListener<>() {
            @Override
            protected void onBroadcast(UpdateBroadcast broadcast) {
                checkWrapper(broadcast.getIdentifier(), broadcast.getKey(), broadcast.getElement());
            }
        });
        this.deleteBroadcaster.registerListener(new BroadcastListener<>() {
            @Override
            protected void onBroadcast(DeleteBroadcast broadcast) {
                checkWrapper(broadcast.getIdentifier(), "deleted", new JsonPrimitive(true));
            }
        });
    }

    public void disable() {
        this.updateBroadcaster.unregisterListeners();
        this.deleteBroadcaster.unregisterListeners();
    }

    protected abstract void onUpdate(T wrapper);

    @SuppressWarnings("unchecked")
    void update(Wrapper wrapper) {
        try {
            onUpdate((T) wrapper);
        } catch (Exception exception) {
            logger.error("Cannot update wrapper " + wrapper.getIdentifier(), exception);
        }
    }

    void checkWrapper(String identifier, String key, JsonElement element) {
        T wrapper = getWrapper(identifier);
        if (wrapper == null) return;
        wrapper.getBackendObject().updateObject(key, element);
        onUpdate(wrapper);
    }

    private T getWrapper(String identifier) {
        return this.wrappers.stream()
                .filter(wrapper -> wrapper.getIdentifier().equals(identifier))
                .findAny().orElse(null);
    }
}
