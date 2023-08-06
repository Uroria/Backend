package com.uroria.backend.service.modules.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public final class CloudAPI {
    private static final Logger logger = LoggerFactory.getLogger("Cloud");

    private final UUID uuid;
    private final String token;
    private final OkHttpClient client;
    private final Int2ObjectArrayMap<String> objects;
    private final WebSocket socket;

    public CloudAPI(String stringUUID, String token) {
        UUID uuid;
        try {
            uuid = UUID.fromString(stringUUID);
        } catch (Exception exception) {
            uuid = UUID.randomUUID();
        }
        this.uuid = uuid;
        this.token = token;
        this.objects = new Int2ObjectArrayMap<>();
        this.client = new OkHttpClient();

        this.socket = null;
        if (true) return;

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url("http://rpr.api.uroria.com:8004/api/v1/ws")
                .build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                logger.debug("Closing websocket connection to reaper gateway. Code: "  + code);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                logger.debug("Received message: " + text);
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                logger.debug("Opened websocket connection to reaper gateway");
                logger.debug(response.message());
                response.headers().forEach(header -> logger.debug(header.component1() + " " + header.component2()));
            }
        };

        this.socket = client.newWebSocket(request, listener);
    }

    public int startServer(int templateId) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("template_id", String.valueOf(templateId))
                .addFormDataPart("iss_uuid", this.uuid.toString())
                .addFormDataPart("description", "A Uroria server")
                .build();

        Request request = new Request.Builder()
                .url("http://rpr.api.uroria.com:8004/api/v1/server/create")
                .method("POST", requestBody)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new RuntimeException("Body empty");
            String string = responseBody.string();
            logger.debug(string);
            JsonObject object = new JsonParser().parse(string).getAsJsonObject();
            JsonElement status = object.get("status");
            if (status == null) throw new RuntimeException("Received status is not available");
            int asInt = status.getAsInt();
            if (asInt == 404) throw new IllegalArgumentException("TemplateId doesn't exist");
            return object.get("sid").getAsInt();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void close() {
        this.socket.close(200, "Closing connection");
    }
}
