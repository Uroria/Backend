package com.uroria.backend.service.modules;

import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.broadcast.BroadcastPoint;
import com.uroria.backend.communication.response.ResponsePoint;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.store.cache.Cache;
import com.uroria.backend.service.store.cache.RedisCache;

public abstract class CachingModule extends BackendModule {
    protected final Cache cache;
    protected final String prefix;

    public CachingModule(BackendServer server, ResponsePoint responsePoint, BroadcastPoint broadcastPoint, String topic, String moduleName, String prefix) {
        super(server, responsePoint, broadcastPoint, topic, moduleName);
        this.cache = new RedisCache(server.getRedisCache());
        this.prefix = prefix;
    }

    public CachingModule(BackendServer server, Communicator communicator, String responseTopic, String broadcastTopic, String topic, String moduleName, String prefix) {
        this(server, new ResponsePoint(communicator, responseTopic), new BroadcastPoint(communicator, broadcastTopic), topic, moduleName, prefix);
    }

    public CachingModule(BackendServer server, String responseTopic, String broadcastTopic, String topic, String moduleName, String prefix) {
        this(server, new ResponsePoint(server.getCommunicator(), responseTopic), new BroadcastPoint(server.getCommunicator(), broadcastTopic), topic, moduleName, prefix);
    }
}
