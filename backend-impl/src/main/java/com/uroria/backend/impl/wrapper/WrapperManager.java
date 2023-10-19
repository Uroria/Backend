package com.uroria.backend.impl.wrapper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Connection;
import com.uroria.are.Application;
import com.uroria.backend.Backend;
import com.uroria.backend.Deletable;
import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.broadcast.DeleteChannel;
import com.uroria.backend.impl.communication.broadcast.RabbitDeleteChannel;
import com.uroria.backend.impl.communication.request.RabbitRequestChannel;
import com.uroria.backend.impl.communication.request.RequestChannel;
import com.uroria.base.event.EventManager;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import org.slf4j.Logger;

@Getter
public abstract class WrapperManager<T extends Wrapper> extends AbstractManager {
    protected final String name;
    private final String identifierKey;
    protected final CommunicationClient client;
    protected final RequestChannel request;
    protected final DeleteChannel delete;
    protected final ObjectSet<T> wrappers;
    protected final EventManager eventManager;

    public WrapperManager(Connection rabbit, Logger logger, String name, String identifierKey) {
        super(rabbit, logger);
        this.name = name;
        this.identifierKey = identifierKey;
        this.client = new CommunicationClient(rabbit, name + "-send", name + "-update");
        this.request = new RabbitRequestChannel(rabbit, name + "-request");
        this.delete = new RabbitDeleteChannel(rabbit, name + "-delete");
        this.wrappers = new ObjectArraySet<>();
        this.eventManager = Backend.getEventManager();
    }

    protected abstract void onUpdate(T wrapper);

    private void update(JsonObject element) {
        JsonElement identifierElement = element.get(this.identifierKey);
        if (identifierElement == null) return;
        String identifier = identifierElement.getAsString();
        boolean delete = false;
        for (T wrapper : wrappers) {
            if (!wrapper.getStringIdentifier().equals(identifier)) continue;
            if (wrapper instanceof Deletable deletable) {
                if (deletable.isDeleted()) {
                    delete = true;
                } else wrapper.refresh();
            }
            onUpdate(wrapper);
            break;
        }
        if (delete) delete(identifier);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        if (Application.isOffline() || Application.isTest()) return;
        this.client.close();
        this.request.close();
        this.wrappers.clear();
    }

    public final void delete(String identifier) {
        this.wrappers.removeIf(wrapper -> wrapper.getStringIdentifier().equals(identifier));
        if (Application.isOffline() || Application.isTest()) return;
        this.client.delete(identifier);
    }

    protected abstract T createWrapper(String identifier);

    protected T getWrapper(String identifier, boolean autoCreate) {
        for (T wrapper : this.wrappers) {
            if (!wrapper.getStringIdentifier().equals(identifier)) continue;
            return wrapper;
        }

        if (Application.isOffline() || Application.isTest()) {
            if (autoCreate) {
                T wrapper = createWrapper(identifier);
                this.wrappers.add(wrapper);
                return wrapper;
            }
            return null;
        }

        byte[] data;
        try {
            InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream();
            output.writeBoolean(autoCreate);
            output.writeUTF(identifier);
            output.close();
            data = output.toByteArray();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        Result<byte[]> result = this.request.requestSync(data, 4000);

        if (result instanceof Result.Problematic<byte[]> problem) {
            logger.error("Cannot request " + name + " " + identifier, problem.getProblem().getError().orElse(new RuntimeException("Unknown error")));
            return null;
        }

        byte[] bytes = result.get();
        if (bytes == null) {
            return null;
        }

        try {
            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(bytes);
            boolean create = input.readBoolean();
            input.close();

            if (!create) return null;

            T wrapper = createWrapper(identifier);
            wrapper.getObject().addProperty(wrapper.getIdentifierKey(), identifier);
            this.wrappers.add(wrapper);
            return wrapper;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
