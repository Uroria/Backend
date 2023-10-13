package com.uroria.backend.service.modules.server;

import com.google.gson.JsonElement;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.CachingModule;


public final class ServerModule extends CachingModule {
    public ServerModule(BackendServer server, String moduleName, String prefix) {
        super(server, moduleName, prefix);
    }

    @Override
    public JsonElement getPart(String identifierKey, Object identifier, String key) {
        return null;
    }

    @Override
    public void checkPart(String identifierKey, Object identifier, String key, JsonElement value) {

    }
}
