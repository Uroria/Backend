package com.uroria.backend.service.modules.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.cache.communication.user.GetUserRequest;
import com.uroria.backend.cache.communication.user.GetUserResponse;
import com.uroria.backend.communication.response.RequestListener;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.SavingModule;
import com.uroria.problemo.result.Result;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class UserModule extends SavingModule {

    public UserModule(BackendServer server) {
        super(server, "user", "UserModule", "user", "users");
        responsePoint.registerResponser(GetUserRequest.class, GetUserResponse.class, "Check", new RequestListener<>() {
            @Override
            protected Optional<GetUserResponse> onRequest(GetUserRequest request) {
                String name = request.getName();
                UUID uuid = request.getUuid();
                if (name == null && uuid == null) return Optional.empty();
                if (name != null && uuid == null) {
                    UUID savedUuid = getUUID(name);
                    if (savedUuid == null) return Optional.empty();
                    return Optional.of(new GetUserResponse(true, savedUuid));
                }
                if (name == null) {
                    JsonElement element = cache.get(prefix + ":" + uuid).get();
                    if (element == null) return Optional.of(new GetUserResponse(false, uuid));
                    return Optional.of(new GetUserResponse(true, uuid));
                }
                return Optional.empty();
            }
        });
    }

    @Override
    protected Optional<PartResponse> request(PartRequest request) {
        String identifier = request.getIdentifier();
        this.cache.set(prefix + ":" + identifier, new JsonPrimitive(identifier), Duration.ofDays(5));
        String key = request.getKey();
        JsonElement part = getPart("uuid", identifier, key);
        if (part.isJsonNull()) return Optional.empty();
        return Optional.of(new PartResponse(identifier, key, part));
    }

    @Override
    protected void update(UpdateBroadcast broadcast) {
        checkPart("uuid", broadcast.getIdentifier(), broadcast.getKey(), broadcast.getElement());
    }

    @Override
    protected void delete(DeleteBroadcast broadcast) {
        String identifier = broadcast.getIdentifier();
        this.cache.delete(prefix + ":" + identifier);
        try {
            String username = getUsername(UUID.fromString(identifier));
            if (username != null) this.cache.delete("username" + username);
        } catch (Exception exception) {
            logger.error("Cannot delete old username from cache of " + identifier);
        }
        this.cache.delete(prefix + ":" + identifier);
        this.db.delete("uuid", identifier);
    }

    public @Nullable String getUsername(UUID uuid) {
        JsonElement part = getPart("uuid", uuid, "username");
        if (part.isJsonNull()) return null;
        return part.getAsString();
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
