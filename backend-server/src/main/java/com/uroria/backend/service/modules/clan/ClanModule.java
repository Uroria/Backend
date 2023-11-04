package com.uroria.backend.service.modules.clan;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.uroria.backend.cache.communication.DeleteBroadcast;
import com.uroria.backend.cache.communication.PartRequest;
import com.uroria.backend.cache.communication.PartResponse;
import com.uroria.backend.cache.communication.UpdateBroadcast;
import com.uroria.backend.cache.communication.clan.CheckClanRequest;
import com.uroria.backend.cache.communication.clan.CheckClanResponse;
import com.uroria.backend.cache.communication.clan.GetClanRequest;
import com.uroria.backend.cache.communication.clan.GetClanResponse;
import com.uroria.backend.communication.response.RequestListener;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.SavingModule;
import com.uroria.problemo.result.Result;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

public final class ClanModule extends SavingModule {

    public ClanModule(BackendServer server) {
        super(server, "clan", "ClanModule", "clan", "clans");
        responsePoint.registerResponser(CheckClanRequest.class, CheckClanResponse.class, "CheckTag", new RequestListener<>() {
            @Override
            protected Optional<CheckClanResponse> onRequest(CheckClanRequest request) {
                String tag = request.getTag();
                String name = getName(tag);
                if (name == null) return Optional.of(new CheckClanResponse(tag, null, false));
                return Optional.of(new CheckClanResponse(tag, name, true));
            }
        });
        responsePoint.registerResponser(GetClanRequest.class, GetClanResponse.class, "CheckName", new RequestListener<>() {
            @Override
            protected Optional<GetClanResponse> onRequest(GetClanRequest request) {
                String name = request.getName();
                if (request.isAutoCreate()) return Optional.of(new GetClanResponse(true));
                JsonElement element = cache.get("clan:" + name).get();
                if (element == null) {
                    element = db.get("name", name, "name").get();
                    if (element == null) return Optional.of(new GetClanResponse(false));
                }
                return Optional.of(new GetClanResponse(true));
            }
        });
    }

    @Override
    protected Optional<PartResponse> request(PartRequest request) {
        String name = request.getIdentifier();
        this.cache.set(prefix + ":" + name, new JsonPrimitive(name), Duration.ofDays(5));
        String key = request.getKey();
        JsonElement part = getPart("name", name, key);
        if (part.isJsonNull()) return Optional.empty();
        return Optional.of(new PartResponse(name, key, part));
    }

    @Override
    protected void update(UpdateBroadcast broadcast) {
        String name = broadcast.getIdentifier();
        this.cache.set("clan:" + name, new JsonPrimitive(name), Duration.ofDays(30));
        String key = broadcast.getKey();
        checkPart("name", name, key, broadcast.getElement());
    }

    @Override
    protected void delete(DeleteBroadcast broadcast) {
        String identifier = broadcast.getIdentifier();
        JsonObject object = this.db.get("name", identifier).get();
        if (object != null) {
            for (String key : object.keySet()) {
                this.cache.delete(prefix + ":" + identifier + ":" + key);
            }
        }
        checkPart("name", identifier, "deleted", new JsonPrimitive(true));
    }

    public @Nullable String getName(String tag) {
        Result<JsonElement> cacheResult = this.cache.get("clantag:" + tag);
        if (cacheResult.isPresent()) {
            JsonElement element = cacheResult.get();
            assert element != null;
            return element.getAsString();
        }
        Result<JsonElement> result = this.db.get("tag", tag, "name");
        if (result.isPresent()) {
            JsonElement element = result.get();
            assert element != null;
            String name = element.getAsString();
            this.cache.set("clantag:" + tag, new JsonPrimitive(name), Duration.ofDays(5));
            return name;
        }
        return null;
    }
}
