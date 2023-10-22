package com.uroria.backend.service.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.uroria.backend.service.BackendServer;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.time.Duration;

public abstract class LocalCachingModule extends CachingModule {
    protected final ObjectSet<Object> allIdentifiers;
    private final Object2ObjectMap<String, JsonElement> localCache;

    public LocalCachingModule(BackendServer server, String moduleName, String prefix) {
        super(server, moduleName, prefix);
        this.allIdentifiers = new ObjectArraySet<>();
        this.localCache = new Object2ObjectArrayMap<>();
    }

    @Override
    public JsonElement getPart(String identifierKey, Object identifier, String key) {
        Result<JsonElement> cacheResult = this.cache.get(prefix + ":" + identifier + ":" + key);
        if (cacheResult.isPresent()) {
            JsonElement element = cacheResult.get();
            if (element == null) return JsonNull.INSTANCE;
            return element;
        }
        JsonElement element = this.localCache.get(prefix + "." + identifier + "." + key);
        if (element != null) {
            this.allIdentifiers.add(identifier);
            Result<Void> result = this.cache.set(prefix + ":" + identifier + ":" + key, element, Duration.ofHours(2));
            if (!result.isProblematic()) {
                logger.info("Added in cache " + key + " of " + identifier);
            } else {
                Problem problem = result.getAsProblematic().getProblem();
                logger.warn("Unable to update in cache " + key + " of " + identifier, problem.getError().orElse(null));
            }
            return element;
        }
        return JsonNull.INSTANCE;
    }

    @Override
    public void checkPart(String identifierKey, Object identifier, String key, JsonElement value) {
        String cacheKey = prefix + ":" + identifier + ":" + key;
        this.cache.delete(cacheKey);
        Result<Void> cacheResult = this.cache.set(cacheKey, value, Duration.ofHours(1));
        this.localCache.put(prefix + "." + identifier + "." + key, value);
        if (key.equals("deleted") && value.getAsBoolean()) {
            this.allIdentifiers.remove(identifier);
        }
        if (!cacheResult.isProblematic()) {
            logger.info("Updated in cache " + key + " of " + identifier);
        } else {
            Problem problem = cacheResult.getAsProblematic().getProblem();
            logger.warn("Unable to update in cache " + key + " of " + identifier, problem.getError().orElse(null));
        }
    }
}
