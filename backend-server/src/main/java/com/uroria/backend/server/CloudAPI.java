package com.uroria.backend.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.UUID;

public final class CloudAPI {
    private final UUID uuid;
    private final OkHttpClient client;
    private final String token;
    public CloudAPI(UUID uuid, String token) {
        this.uuid = uuid;
        this.client = new OkHttpClient.Builder().build();
        this.token = token;
    }

    public CloudAPI(String stringUUID, String token) {
        UUID uuid;
        try {
            uuid = UUID.fromString(stringUUID);
        } catch (Exception exception) {
            uuid = UUID.randomUUID();
        }
        this.uuid = UUID.randomUUID();
        this.client = new OkHttpClient.Builder().build();
        this.token = token;
    }

    public int startServer(int templateId, String url) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("template_id", String.valueOf(templateId))
                .addFormDataPart("iss_uuid", this.uuid.toString())
                .addFormDataPart("description", "A Uroria server")
                .build();

        Request request = new Request.Builder()
                .url(url + "/server/create")
                .method("POST", requestBody)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new RuntimeException("Body empty");
            JsonObject object = new JsonParser().parse(responseBody.string()).getAsJsonObject();
            return object.get("sid").getAsInt();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
