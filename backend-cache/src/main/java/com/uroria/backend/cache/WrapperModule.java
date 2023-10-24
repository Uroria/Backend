package com.uroria.backend.cache;

import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.communication.broadcast.BroadcastListener;
import com.uroria.backend.communication.broadcast.BroadcastPoint;
import com.uroria.backend.communication.broadcast.Broadcaster;
import com.uroria.backend.communication.response.RequestListener;
import com.uroria.backend.communication.response.ResponsePoint;
import com.uroria.backend.communication.response.Responser;
import org.slf4j.Logger;

import java.util.Optional;

public abstract class WrapperModule {
    protected final Logger logger;
    protected final ResponsePoint responsePoint;
    protected final BroadcastPoint broadcastPoint;
    protected final Responser<PartRequest, PartResponse> partRequester;
    protected final Broadcaster<UpdateBroadcast> updateBroadcaster;
    protected final Broadcaster<DeleteBroadcast> deleteBroadcaster;

    public WrapperModule(Logger logger, ResponsePoint responsePoint, BroadcastPoint broadcastPoint, String topic) {
        this.logger = logger;
        this.responsePoint = responsePoint;
        this.broadcastPoint = broadcastPoint;
        this.partRequester = responsePoint.registerResponser(PartRequest.class, PartResponse.class, topic + "-part", new RequestListener<>() {
            @Override
            protected Optional<PartResponse> onRequest(PartRequest request) {
                return request(request);
            }
        });
        this.updateBroadcaster = broadcastPoint.registerBroadcaster(UpdateBroadcast.class, topic + "-part");
        this.deleteBroadcaster = broadcastPoint.registerBroadcaster(DeleteBroadcast.class, topic + "-delete");
        this.updateBroadcaster.registerListener(new BroadcastListener<>() {
            @Override
            protected void onBroadcast(UpdateBroadcast broadcast) {
                update(broadcast);
            }
        });
        this.deleteBroadcaster.registerListener(new BroadcastListener<>() {
            @Override
            protected void onBroadcast(DeleteBroadcast broadcast) {
                delete(broadcast);
            }
        });
    }

    protected abstract Optional<PartResponse> request(PartRequest request);

    protected abstract void update(UpdateBroadcast broadcast);

    protected abstract void delete(DeleteBroadcast broadcast);
}
