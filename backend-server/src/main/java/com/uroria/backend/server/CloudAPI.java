package com.uroria.backend.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;

public final class CloudAPI {
    private final UUID uuid;
    private final OkHttpClient client;
    private final String token;

    public CloudAPI(String stringUUID, String token) {
        UUID uuid;
        try {
            uuid = UUID.fromString(stringUUID);
        } catch (Exception exception) {
            uuid = UUID.randomUUID();
        }
        this.uuid = UUID.randomUUID();
        this.token = token;
        this.client = new OkHttpClient();
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
            Uroria.getLogger().debug(string);
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

    public InetSocketAddress getAddress(int serverId, int maxRetries) {
        Request request = new Request.Builder()
                .url("http://rpr.api.uroria.com:8004/api/v1/server/" + serverId)
                .method("GET", null)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        InetSocketAddress address = null;
        int currentTry = 0;
        while (address == null) {
            if (currentTry++ > maxRetries) break;

            try (Response response = client.newCall(request).execute()) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) throw new RuntimeException("Body cannot be null");
                String string = responseBody.string();

                JsonObject object = new JsonParser().parse(string).getAsJsonObject().getAsJsonObject("server");

                JsonElement ipObject = object.get("ip");
                JsonElement portObject = object.get("port");
                if (ipObject.isJsonNull() || portObject.isJsonNull()) continue;
                String ipString = ipObject.getAsString();
                int port = portObject.getAsInt();

                address = new InetSocketAddress(ipString, port);
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }

            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {}
        }
        return address;
    }
}
