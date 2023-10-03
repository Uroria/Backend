package com.uroria.backend.service.modules.perm;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.communication.cache.Cache;
import com.uroria.backend.service.communication.cache.RedisCache;
import com.uroria.backend.service.communication.database.Database;
import com.uroria.backend.service.communication.database.MongoDatabase;
import com.uroria.backend.service.modules.BackendModule;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;

import java.time.Duration;

public final class PermModule extends BackendModule {
    private final Database db;
    private final Cache cache;

    private final PermGroupObjectThread objectThread;
    private final PermGroupUpdateThread updateThread;
    private final PermGroupPartThread partThread;

    public PermModule(BackendServer server) {
        super(server, "PermModule");
        this.db = new MongoDatabase(server.getDatabase().getCollection("perm_groups"));
        this.cache = new RedisCache(server.getRedisCache());
        this.objectThread = new PermGroupObjectThread(this);
        this.updateThread = new PermGroupUpdateThread(this);
        this.partThread = new PermGroupPartThread(this);
    }

    @Override
    protected void enable() throws Exception {
        this.objectThread.start();
        this.updateThread.start();
        this.partThread.start();
    }

    @Override
    protected void disable() throws Exception {
        this.objectThread.getResponseChannel().close();
        this.updateThread.getUpdateChannel().close();
        this.partThread.getResponseChannel().close();
    }

    public JsonElement getPart(String name, String key) {
        Result<JsonElement> cacheResult = this.cache.get("perm_group:" + name + ":" + key);
        if (cacheResult.isPresent()) {
            JsonElement element = cacheResult.get();
            if (element == null) return JsonNull.INSTANCE;
            return element;
        }
        Result<JsonElement> result = this.db.get("name", name, key);
        if (result.isPresent()) {
            JsonElement element = result.get();
            if (element == null) return JsonNull.INSTANCE;
            Result<Void> setResult = this.cache.set("perm_group:" + name + ":" + key, element, Duration.ofDays(5));
            if (!setResult.isProblematic()) {
                logger.info("Added in cache " + key + " of perm-group" + name);
            } else {
                Problem problem = setResult.getAsProblematic().getProblem();
                logger.warn("Unable to update in cache " + key + " of perm-group " + name, problem.getError().orElse(null));
            }
            return element;
        }
        return JsonNull.INSTANCE;
    }

    public void checkPart(String name, String key, JsonElement value) {
        String cacheKey = "perm_group:" + name + ":" + key;
        this.cache.delete(cacheKey);
        Result<JsonElement> cacheResult = this.cache.get(cacheKey);
        Result<Void> result = this.db.set("name", name, key, value);
        if (!cacheResult.isProblematic()) {
            logger.info("Updated in cache " + key + " of perm-group " + name);
        } else {
            Problem problem = cacheResult.getAsProblematic().getProblem();
            logger.warn("Unable to update in cache " + key + " of perm-group " + name, problem.getError().orElse(null));
        }
        if (!result.isProblematic()) {
            logger.info("Updated in database " + key + " of perm-group " + name);
        } else {
            Problem problem = result.getAsProblematic().getProblem();
            logger.warn("Unable to update in database " + key + " of perm-group " + name, problem.getError().orElse(null));
        }
    }
}
