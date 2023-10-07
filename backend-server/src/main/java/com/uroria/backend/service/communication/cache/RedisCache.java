package com.uroria.backend.service.communication.cache;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.NonNull;

import java.time.Duration;

public class RedisCache implements Cache {
    private final RedisCommands<String, String> cache;

    public RedisCache(StatefulRedisConnection<String, String> connection) {
        this.cache = connection.sync();
    }

    @Override
    public final Result<Void> set(@NonNull String key, @NonNull JsonElement value, @NonNull Duration lifetime) {
        try {
            JsonObject object = new JsonObject();
            object.add("value", value);
            this.cache.set(key, object.toString(), SetArgs.Builder.ex(lifetime));
            return Result.none();
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public final Result<JsonElement> get(@NonNull String key) {
        try {
            return Result.of(JsonParser.parseString(this.cache.get(key)).getAsJsonObject().get("value"));
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public final void delete(String key) {
        if (key == null) return;
        this.cache.del(key);
    }

    @Override
    public final void delete(String... keys) {
        if (keys == null) return;
        this.cache.del(keys);
    }
}
