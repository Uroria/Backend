package com.uroria.backend.service.store.database;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.uroria.backend.cache.utils.GsonUtils;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class MongoDatabase implements Database {
    private static final Logger logger = LoggerFactory.getLogger("Database");
    private final MongoCollection<Document> db;

    public MongoDatabase(@NonNull MongoCollection<Document> db) {
        this.db = db;
    }

    private synchronized Result<Void> set(@NonNull String targetKey, @NonNull JsonPrimitive targetKeyValue, @NonNull String key, @NonNull JsonElement value) {
        Result<JsonObject> result = get(targetKey, GsonUtils.toObject(targetKeyValue));
        if (!result.isPresent()) {
            JsonObject object = new JsonObject();
            object.add(targetKey, targetKeyValue);
            object.add(key, value);
            return set(targetKey, targetKeyValue, object);
        }
        JsonObject object = result.get();
        if (object == null) return Result.problem(Problem.plain("Something is null that cannot be null"));
        object.remove(key);
        object.add(key, value);
        return set(targetKey, targetKeyValue, object);
    }

    @Override
    public Result<Collection<JsonObject>> getAll() {
        try {
            Collection<JsonObject> objects = new ObjectArraySet<>();
            MongoCursor<Document> iterator = this.db.find().iterator();
            while (iterator.hasNext()) {
                Document document = iterator.next();
                JsonElement element = JsonParser.parseString(document.toJson());
                objects.add(element.getAsJsonObject());
            }
            iterator.close();
            return Result.some(objects);
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public synchronized Result<Void> set(@NonNull String key, @NonNull JsonElement value) {
        JsonObject object = new JsonObject();
        object.add(key, value);
        if (this.db.insertOne(Document.parse(object.toString())).wasAcknowledged()) {
            logger.debug("Inserted new document with key " + key + " and value " + value);
            return Result.none();
        }
        return Result.problem(Problem.plain("Cannot insert :/"));
    }

    @Override
    public final Result<Void> set(@NonNull String targetKey, @NonNull Number targetKeyValue, @NonNull String key, @NonNull JsonElement value) {
        return set(targetKey, new JsonPrimitive(targetKeyValue), key, value);
    }

    @Override
    public Result<Void> set(@NonNull String targetKey, @NonNull String targetKeyValue, @NonNull String key, @NonNull JsonElement value) {
        return set(targetKey, new JsonPrimitive(targetKeyValue), key, value);
    }

    @Override
    public Result<Void> set(@NonNull String targetKey, boolean targetKeyValue, @NonNull String key, @NonNull JsonElement value) {
        return set(targetKey, new JsonPrimitive(targetKeyValue), key, value);
    }

    @Override
    public Result<Void> set(@NonNull String key, @NonNull Number keyValue, @NonNull JsonObject object) {
        return set(key, new JsonPrimitive(keyValue), object);
    }

    public final synchronized Result<Void> set(@NonNull String key, @NonNull JsonPrimitive keyValue, @NonNull JsonObject object) {
        Object realKeyValue = GsonUtils.toObject(keyValue);
        synchronized (this.db) {
            Result<JsonObject> result = get(key, realKeyValue);
            if (!result.isPresent()) {
                try {
                    Document document = Document.parse(object.toString());
                    if (this.db.insertOne(document).wasAcknowledged()) {
                        logger.debug("Inserted new document for key " + key + " with value " + keyValue);
                        return Result.none();
                    }
                } catch (Exception exception) {
                    logger.warn("Cannot insert new document for key " + key + " with value " + keyValue, exception);
                    return Result.problem(Problem.error(exception));
                }
                return Result.problem(Problem.plain("Unable to insert " + key + " for " + keyValue + " of " + db.getNamespace()));
            }
            try {
                if (this.db.replaceOne(Filters.eq(key, realKeyValue), Document.parse(object.toString())).wasAcknowledged()) {
                    logger.debug("Replaced document with key " + key + " with value " + keyValue);
                    return Result.none();
                }
                return Result.problem(Problem.plain("Unable to replace " + key + " for " + keyValue + " of " + db.getNamespace()));
            } catch (Exception exception) {
                logger.warn("Cannot replace new document for key " + key + " with value " + keyValue, exception);
                return Result.problem(Problem.error(exception));
            }
        }
    }

    @Override
    public Result<Void> set(@NonNull String key, @NonNull String keyValue, @NonNull JsonObject object) {
        return set(key, new JsonPrimitive(keyValue), object);
    }

    @Override
    public Result<Void> set(@NonNull String key, boolean keyValue, @NonNull JsonObject object) {
        return set(key, new JsonPrimitive(keyValue), object);
    }

    @Override
    public Result<JsonObject> get(@NonNull String key, @NonNull Number keyValue) {
        return get(key, (Object) keyValue);
    }

    @Override
    public Result<JsonObject> get(@NonNull String key, @NonNull String keyValue) {
        return get(key, (Object) keyValue);
    }

    @Override
    public Result<JsonObject> get(@NonNull String key, boolean keyValue) {
        return get(key, (Object) keyValue);
    }

    @Override
    public Result<JsonElement> get(@NonNull String valueKey, @NonNull Number value, @NonNull String key) {
        return get(valueKey, new JsonPrimitive(value), key);
    }

    @Override
    public Result<JsonElement> get(@NonNull String valueKey, String value, @NonNull String key) {
        return get(valueKey, new JsonPrimitive(value), key);
    }

    @Override
    public Result<JsonElement> get(@NonNull String valueKey, boolean value, @NonNull String key) {
        return get(valueKey, new JsonPrimitive(value), key);
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull Number value) {
        return get(key, condition, new JsonPrimitive(value));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull String value) {
        return get(key, condition, new JsonPrimitive(value));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, boolean value) {
        return get(key, condition, new JsonPrimitive(value));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull Number value, @NonNull Operator operator, @NonNull Number secondValue) {
        return get(key, condition, new JsonPrimitive(value), operator, new JsonPrimitive(secondValue));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull Number value, @NonNull Operator operator, @NonNull String secondValue) {
        return get(key, condition, new JsonPrimitive(value), operator, new JsonPrimitive(secondValue));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull Number value, @NonNull Operator operator, boolean secondValue) {
        return get(key, condition, new JsonPrimitive(value), operator, new JsonPrimitive(secondValue));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull String value, @NonNull Operator operator, @NonNull Number secondValue) {
        return get(key, condition, new JsonPrimitive(value), operator, new JsonPrimitive(secondValue));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull String value, @NonNull Operator operator, @NonNull String secondValue) {
        return get(key, condition, new JsonPrimitive(value), operator, new JsonPrimitive(secondValue));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull String value, @NonNull Operator operator, boolean secondValue) {
        return get(key, condition, new JsonPrimitive(value), operator, new JsonPrimitive(secondValue));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, boolean value, @NonNull Operator operator, @NonNull Number secondValue) {
        return get(key, condition, new JsonPrimitive(value), operator, new JsonPrimitive(secondValue));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, boolean value, @NonNull Operator operator, @NonNull String secondValue) {
        return get(key, condition, new JsonPrimitive(value), operator, new JsonPrimitive(secondValue));
    }

    @Override
    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, boolean value, @NonNull Operator operator, boolean secondValue) {
        return get(key, condition, new JsonPrimitive(value), operator, new JsonPrimitive(secondValue));
    }

    @Override
    public Result<Void> delete(@NonNull String key, @NonNull Number keyValue) {
        return delete(key, (Object) keyValue);
    }

    @Override
    public Result<Void> delete(@NonNull String key, boolean keyValue) {
        return delete(key, (Object) keyValue);
    }

    @Override
    public Result<Void> delete(@NonNull String key, @NonNull String keyValue) {
        return delete(key, (Object) keyValue);
    }

    public Result<JsonObject> get(@NonNull String key, @NonNull Object keyValue) {
        Document document = this.db.find(Filters.eq(key, keyValue)).first();
        if (document == null) return Result.none();
        try {
            JsonElement element = JsonParser.parseString(document.toJson());
            return Result.of(element.getAsJsonObject());
        } catch (Exception exception) {
            logger.warn("Cannot parse document for key " + key + " with value " + keyValue, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    public Result<JsonElement> get(@NonNull String valueKey, @NonNull JsonPrimitive value, @NonNull String key) {
        JsonObject object = get(valueKey, GsonUtils.toObject(value)).get();
        if (object != null) return Result.of(object.get(key));
        return Result.none();
    }

    public final Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull JsonPrimitive value) {
        switch (condition) {
            case GREATER_THAN -> {
                return getObjects(Filters.gt(key, value));
            }
            case LESS_THAN -> {
                return getObjects(Filters.lt(key, value));
            }
        }
        return Result.none();
    }

    public Result<Collection<JsonObject>> get(@NonNull String key, @NonNull Condition condition, @NonNull JsonPrimitive value, @NonNull Operator operator, JsonPrimitive secondValue) {
        switch (condition) {
            case GREATER_THAN -> {
                switch (operator) {
                    case BUT_NOT -> {
                        return getObjects(Filters.and(Filters.gt(key, value), Filters.ne(key, secondValue)));
                    }
                    case BUT_LESS_THAN -> {
                        return getObjects(Filters.and(Filters.gt(key, value), Filters.lt(key, secondValue)));
                    }
                    case BUT_GREATER_THAN -> {
                        return getObjects(Filters.and(Filters.gt(key, value), Filters.gt(key, secondValue)));
                    }
                }
            }
            case LESS_THAN -> {
                switch (operator) {
                    case BUT_NOT -> {
                        return getObjects(Filters.and(Filters.lt(key, value), Filters.ne(key, secondValue)));
                    }
                    case BUT_LESS_THAN -> {
                        return getObjects(Filters.and(Filters.lt(key, value), Filters.lt(key, secondValue)));
                    }
                    case BUT_GREATER_THAN -> {
                        return getObjects(Filters.and(Filters.lt(key, value), Filters.gt(key, secondValue)));
                    }
                }
            }
        }
        return Result.none();
    }

    private Result<Collection<JsonObject>> getObjects(@NonNull Bson condition) {
        Collection<JsonObject> objects = new ObjectArraySet<>();
        MongoCursor<Document> iterator = this.db.find(condition).iterator();
        while (iterator.hasNext()) {
            Document next = iterator.next();
            try {
                JsonObject object = JsonParser.parseString(next.toJson()).getAsJsonObject();
                objects.add(object);
            } catch (Exception exception) {
                iterator.close();
                return Result.problem(Problem.error(exception));
            }
        }
        iterator.close();
        return Result.some(objects);
    }

    public final synchronized Result<Void> delete(@NonNull String key, @NonNull Object keyValue) {
        synchronized (this.db) {
            logger.debug("Deleted document with key " + key + " and key-value " + keyValue);
            if (this.db.deleteOne(Filters.eq(key, keyValue)).wasAcknowledged()) {
                return Result.none();
            }
            return Result.problem(Problem.plain("Unable to delete " + key + "for " + keyValue + " of " + db.getNamespace()));
        }
    }
}
