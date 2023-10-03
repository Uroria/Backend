package com.uroria.backend.service.modules.user;

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
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.UUID;

public final class UserModule extends BackendModule {
    private final Database db;
    private final Cache cache;

    private final UserObjectThread objectThread;
    private final UserPartThread partThread;
    private final UserUpdateThread updateThread;

    public UserModule(BackendServer server) {
        super(server, "UserModule");
        this.db = new MongoDatabase(server.getDatabase().getCollection("users"));
        this.cache = new RedisCache(server.getRedisCache());
        this.objectThread = new UserObjectThread(this);
        this.partThread = new UserPartThread(this);
        this.updateThread = new UserUpdateThread(this);
    }

    @Override
    protected void enable() throws Exception {
        this.objectThread.start();
        this.partThread.start();
        this.updateThread.start();
    }

    @Override
    protected void disable() throws Exception {
        this.partThread.getResponseChannel().close();
        this.objectThread.getResponseChannel().close();
        this.updateThread.getUpdateChannel().close();
    }

    public JsonElement getPart(UUID uuid, String key) {
        Result<JsonElement> cacheResult = this.cache.get("user:" + uuid + ":" + key);
        if (cacheResult.isPresent()) {
            JsonElement element = cacheResult.get();
            if (element == null) return JsonNull.INSTANCE;
            return element;
        }
        Result<JsonElement> result = this.db.get("uuid", uuid.toString(), key);
        if (result.isPresent()) {
            JsonElement element = result.get();
            if (element == null) return JsonNull.INSTANCE;
            Result<Void> setResult = this.cache.set("user:" + uuid + ":" + key, element, Duration.ofDays(1));
            if (!setResult.isProblematic()) {
                logger.info("Added in cache " + key + " of user " + uuid);
            } else {
                Problem problem = setResult.getAsProblematic().getProblem();
                logger.warn("Unable to update in cache " + key + " of user " + uuid, problem.getError().orElse(null));
            }
            return element;
        }
        return JsonNull.INSTANCE;
    }

    public void checkPart(UUID uuid, String key, JsonElement value) {
        String cacheKey = "user:" + uuid + ":" + key;
        this.cache.delete(cacheKey);
        Result<Void> cacheResult = this.cache.set(cacheKey, value, Duration.ofHours(8));
        Result<Void> result = this.db.set("uuid", uuid.toString(), key, value);
        if (!cacheResult.isProblematic()) {
            logger.info("Updated in cache " + key + " of user " + uuid);
        } else {
            Problem problem = cacheResult.getAsProblematic().getProblem();
            logger.warn("Unable to update in cache " + key + " of user " + uuid, problem.getError().orElse(null));
        }
        if (!result.isProblematic()) {
            logger.info("Updated in database " + key + " of user " + uuid);
        } else {
            Problem problem = result.getAsProblematic().getProblem();
            logger.warn("Unable to update in database " + key + " of user " + uuid, problem.getError().orElse(null));
        }
    }

    public @Nullable UUID getUUID(String username) {
        Result<JsonElement> cacheResult = this.cache.get("username:" + username);
        if (cacheResult.isPresent()) {
            return fromElement(cacheResult.get());
        }
        Result<JsonElement> result = this.db.get("username", username, "uuid");
        if (result.isPresent()) {
            return fromElement(result.get());
        }
        return null;
    }

    private UUID fromElement(JsonElement element) {
        try {
            if (element == null) return null;
            return UUID.fromString(element.getAsString());
        } catch (Exception exception) {
            this.logger.error("Unable to convert element " + element + " to uuid", exception);
            return null;
        }
    }
}
