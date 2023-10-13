package com.uroria.backend.service.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.communication.database.Database;
import com.uroria.backend.service.communication.database.MongoDatabase;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;

import java.time.Duration;

public abstract class SavingModule extends CachingModule {
    protected final Database db;

    public SavingModule(BackendServer server, String moduleName, String database) {
        super(server, moduleName, database);
        this.db = new MongoDatabase(server.getDatabase().getCollection(database));
    }

    @Override
    public final JsonElement getPart(String identifierKey, Object identifier, String key) {
        Result<JsonElement> cacheResult = this.cache.get(prefix + ":" + identifier + ":" + key);
        if (cacheResult.isPresent()) {
            JsonElement element = cacheResult.get();
            if (element == null) return JsonNull.INSTANCE;
            return element;
        }
        Result<JsonElement> result = this.db.get(identifierKey, identifier.toString(), key);
        if (result.isPresent()) {
            JsonElement element = result.get();
            if (element == null) return JsonNull.INSTANCE;
            Result<Void> setResult = this.cache.set(prefix + ":" + identifier + ":" + key, element, Duration.ofDays(1));
            if (!setResult.isProblematic()) {
                logger.info("Added in cache " + key + " of " + identifier);
            } else {
                Problem problem = setResult.getAsProblematic().getProblem();
                logger.warn("Unable to update in cache " + key + " of " + identifier, problem.getError().orElse(null));
            }
            return element;
        }
        return JsonNull.INSTANCE;
    }

    @Override
    public final void checkPart(String identifierKey, Object identifier, String key, JsonElement value) {
        String cacheKey = prefix + ":" + identifier + ":" + key;
        this.cache.delete(cacheKey);
        Result<Void> cacheResult = this.cache.set(cacheKey, value, Duration.ofHours(8));
        Result<Void> result = this.db.set(identifierKey, identifier.toString(), key, value);
        if (!cacheResult.isProblematic()) {
            logger.info("Updated in cache " + key + " of " + identifier);
        } else {
            Problem problem = cacheResult.getAsProblematic().getProblem();
            logger.warn("Unable to update in cache " + key + " of " + identifier, problem.getError().orElse(null));
        }
        if (!result.isProblematic()) {
            logger.info("Updated in database " + key + " of " + identifier);
        } else {
            Problem problem = result.getAsProblematic().getProblem();
            logger.warn("Unable to update in database " + key + " of " + identifier, problem.getError().orElse(null));
        }
    }

    public Database getDatabase() {
        return this.db;
    }
}
