package com.uroria.backend.service.store.cache;

import com.google.gson.JsonElement;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.time.Duration;

public interface Cache {

    Result<Void> set(@NonNull String key, @NonNull JsonElement value, @NonNull Duration lifetime);

    Result<JsonElement> get(@NonNull String key);

    void delete(String key);

    void delete(String... keys);

    void clear();
}
