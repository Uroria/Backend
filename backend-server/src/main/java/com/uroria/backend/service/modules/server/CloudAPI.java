package com.uroria.backend.service.modules.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.uroria.backend.utils.ThreadUtils;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.UUID;

public final class CloudAPI {
    private static final Logger logger = LoggerFactory.getLogger("Cloud");
    private static final JsonParser parser = new JsonParser();

    private final UUID uuid;
    private final String token;
    private final OkHttpClient client;
    private final Int2ObjectArrayMap<InetSocketAddress> responses;
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
        this.responses = new Int2ObjectArrayMap<>();
        this.client = new OkHttpClient();

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + token)
                .url("http://rpr.api.uroria.com:8004/api/v1/ws")
                .build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                logger.info("Closing websocket connection to reaper gateway. Code: "  + code);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String json) {
                logger.debug(json);
                JsonObject object = parser.parse(json).getAsJsonObject();
                JsonElement eventElement = object.get("event");
                if (eventElement == null) return;
                String eventString = eventElement.getAsString();
                if (eventString.equals("SERVER_STARTED")) {
                    JsonElement idElement = object.get("server_id");
                    if (idElement == null) {
                        logger.warn("Somehow the server has no id?! " + json);
                        return;
                    }
                    int id = idElement.getAsInt();
                    logger.info("Server " + id + " started. " + json);

                    JsonElement hostElement = object.get("hostname");
                    if (hostElement == null) {
                        logger.warn("Hostname element is not present for " + id);
                        return;
                    }
                    String hostname = hostElement.getAsString();

                    JsonElement portElement = object.get("port");
                    if (portElement == null) {
                        logger.warn("Port element is not present for " + id);
                        return;
                    }
                    int port = portElement.getAsInt();

                    logger.info("Server " + id + " started on " + hostname + ":" + port);

                    responses.put(id, new InetSocketAddress(hostname, port));
                    return;
                }
                if (eventString.equals("SERVER_STOPPED")) {
                    JsonElement idElement = object.get("server_id");
                    if (idElement == null) {
                        logger.warn("Somehow the server has no id?! " + json);
                        return;
                    }
                    int id = idElement.getAsInt();
                    logger.info("Server " + id + " stopped. " + json);
                    responses.remove(id);
                }
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                logger.info("Opened websocket connection to reaper gateway");
            }
        };

        this.socket = client.newWebSocket(request, listener);
    }

    public int startServer(int templateId) throws RuntimeException {
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
            logger.info("Server start response of " + templateId + ". " + string);
            JsonObject object = parser.parse(string).getAsJsonObject();
            JsonElement status = object.get("status");
            if (status == null) throw new RuntimeException("Received status is not available");
            int asInt = status.getAsInt();
            if (asInt == 404) throw new IllegalArgumentException("TemplateId doesn't exist");
            return object.get("sid").getAsInt();
        } catch (Exception exception) {
            throw new RuntimeException("Something went wrong while trying to create a server!", exception);
        }
    }

    public @Nullable InetSocketAddress getAddress(int id, int timeout) {
        long start = System.currentTimeMillis();
        try {
            while (true) {
                if ((System.currentTimeMillis() - start) > timeout) break;
                InetSocketAddress address = this.responses.get(id);
                if (address == null) {
                    ThreadUtils.sleep(500);
                    continue;
                }
                return address;
            }
        } catch (Exception exception) {
            logger.error("Cannot read address", exception);
            return null;
        }
        return null;
    }

    public void close() {
        this.socket.close(1000, "Closing connection");
    }
}
