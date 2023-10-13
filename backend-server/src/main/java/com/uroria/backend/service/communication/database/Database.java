package com.uroria.backend.service.communication.database;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.util.Collection;

public interface Database {

    Result<Collection<JsonObject>> getAll();

    Result<Void> set(@NonNull String targetKey, @NonNull Number targetKeyValue, @NonNull String key, @NonNull JsonElement value);

    Result<Void> set(@NonNull String targetKey, @NonNull String targetKeyValue, @NonNull String key, @NonNull JsonElement value);

    Result<Void> set(@NonNull String targetKey, boolean targetKeyValue, @NonNull String key, @NonNull JsonElement value);

    Result<Void> set(@NonNull String key, @NonNull Number keyValue, @NonNull JsonObject object);

    Result<Void> set(@NonNull String key, @NonNull String keyValue, @NonNull JsonObject object);

    Result<Void> set(@NonNull String key, boolean keyValue, @NonNull JsonObject object);

    Result<JsonObject> get(@NonNull String key, @NonNull Number keyValue);

    Result<JsonObject> get(@NonNull String key, @NonNull String keyValue);

    Result<JsonObject> get(@NonNull String key, boolean keyValue);

    Result<JsonElement> get(@NonNull String valueKey, @NonNull Number value, @NonNull String key);

    Result<JsonElement> get(@NonNull String valueKey, String value, @NonNull String key);

    Result<JsonElement> get(@NonNull String valueKey, boolean value, @NonNull String key);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull Number value);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull String value);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, boolean value);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull Number value, @NonNull Operator operator, @NonNull Number secondValue);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull Number value, @NonNull Operator operator, @NonNull String secondValue);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull Number value, @NonNull Operator operator, boolean secondValue);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull String value, @NonNull Operator operator, @NonNull Number secondValue);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull String value, @NonNull Operator operator, @NonNull String secondValue);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull String value, @NonNull Operator operator, boolean secondValue);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, boolean value, @NonNull Operator operator, @NonNull Number secondValue);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, boolean value, @NonNull Operator operator, @NonNull String secondValue);

    Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, boolean value, @NonNull Operator operator, boolean secondValue);

    Result<Void> delete(@NonNull String key, @NonNull Number keyValue);

    Result<Void> delete(@NonNull String key, boolean keyValue);

    Result<Void> delete(@NonNull String key, @NonNull String keyValue);
}
