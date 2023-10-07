package com.uroria.backend.service.modules;

import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.communication.cache.Cache;
import com.uroria.backend.service.communication.cache.RedisCache;

public abstract class CachingModule extends BackendModule {
    protected final Cache cache;
    protected final String prefix;

    public CachingModule(BackendServer server, String moduleName, String prefix) {
        super(server, moduleName);
        this.cache = new RedisCache(server.getRedisCache());
        this.prefix = prefix;
    }
}
