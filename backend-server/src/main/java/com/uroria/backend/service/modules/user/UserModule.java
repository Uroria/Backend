package com.uroria.backend.service.modules.user;

import com.google.gson.JsonElement;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.SavingModule;
import com.uroria.problemo.result.Result;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class UserModule extends SavingModule {
    private final UserObjectThread objectThread;
    private final UserPartThread partThread;
    private final UserUpdateThread updateThread;

    public UserModule(BackendServer server) {
        super(server, "UserModule", "users");
        this.objectThread = new UserObjectThread(this);
        this.partThread = new UserPartThread(this);
        this.updateThread = new UserUpdateThread(this);
    }

    @Override
    protected void enable() {
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
