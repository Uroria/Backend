package com.uroria.backend.service.modules;

import com.google.gson.JsonElement;
import com.uroria.backend.cache.WrapperModule;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.broadcast.BroadcastPoint;
import com.uroria.backend.communication.request.RequestPoint;
import com.uroria.backend.communication.response.ResponsePoint;
import com.uroria.backend.service.BackendServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BackendModule extends WrapperModule implements ControllableModule {
    protected final BackendServer server;
    protected final String moduleName;

    public BackendModule(BackendServer server, ResponsePoint responsePoint, BroadcastPoint broadcastPoint, String topic, String moduleName) {
        super(LoggerFactory.getLogger(moduleName), responsePoint, broadcastPoint, topic);
        this.server = server;
        this.moduleName = moduleName;
    }

    public BackendModule(BackendServer server, Communicator communicator, String responseTopic, String broadcastTopic, String topic, String moduleName) {
        this(server, new ResponsePoint(communicator, responseTopic), new BroadcastPoint(communicator, broadcastTopic), topic, moduleName);
    }

    public abstract JsonElement getPart(String identifierKey, Object identifier, String key);

    public abstract void checkPart(String identifierKey, Object identifier, String key, JsonElement value);

    public final Logger getLogger() {
        return logger;
    }

    @Override
    public final String getModuleName() {
        return moduleName;
    }

    public final BackendServer getServer() {
        return server;
    }
}
