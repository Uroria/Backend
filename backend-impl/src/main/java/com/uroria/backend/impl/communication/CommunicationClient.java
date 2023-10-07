package com.uroria.backend.impl.communication;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Connection;
import com.uroria.backend.impl.communication.broadcast.RabbitUpdateChannel;
import com.uroria.backend.impl.communication.broadcast.UpdateChannel;
import com.uroria.backend.impl.communication.request.RabbitRequestChannel;
import com.uroria.backend.impl.communication.request.RequestChannel;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class CommunicationClient implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger("CommunicationWrapper");

    private final UpdateChannel update;
    private final RequestChannel request;

    private final Object2ObjectMap<String, JsonObject> objects;

    private final Consumer<JsonObject> updateConsumer;

    public CommunicationClient(@NonNull Connection rabbit, @NonNull String requestTopic, @NonNull String updateTopic, Consumer<JsonObject> updateConsumer) {
        this.update = new RabbitUpdateChannel(rabbit, updateTopic);
        this.request = new RabbitRequestChannel(rabbit, requestTopic);
        this.objects = new Object2ObjectArrayMap<>();
        this.updateConsumer = updateConsumer;
        UpdateThread updateThread = new UpdateThread(this);
        updateThread.start();
    }

    public CommunicationClient(@NonNull Connection rabbit, @NonNull String requestTopic, @NonNull String updateTopic) {
        this(rabbit, requestTopic, updateTopic, ignored -> {});
    }

    public void delete(String identifier) {
        this.objects.remove(identifier);
    }

    JsonObject newObject(String identifier) {
        JsonObject object = this.objects.get(identifier);
        if (object == null) {
            object = new JsonObject();
            this.objects.put(identifier, object);
        }
        return object;
    }

    void removeObject(String identifier) {
        this.objects.remove(identifier);
    }

    Result<JsonElement> request(CommunicationWrapper wrapper, String key, int timeout) {
        byte[] data;
        try {
            BackendOutputStream output = new BackendOutputStream();
            output.writeUTF(wrapper.getIdentifier());
            output.writeUTF(key);
            output.close();
            data = output.toByteArray();
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
        Result<byte[]> result = this.request.requestSync(data, timeout);
        if (result.isProblematic()) {
            return Result.problem(result.getAsProblematic().getProblem());
        }
        byte[] response = result.get();
        if (response == null) return Result.none();
        try {
            BackendInputStream input = new BackendInputStream(response);
            JsonElement element = input.readJsonElement();
            input.close();
            return Result.some(element);
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    void update(CommunicationWrapper wrapper, String key, JsonElement value) {
        String identifier = wrapper.getIdentifier();
        try {
            BackendOutputStream output = new BackendOutputStream();
            output.writeUTF(identifier);
            output.writeUTF(key);
            output.writeJsonElement(value);
            output.close();

            this.update.updateAsync(output.toByteArray());

            JsonObject object = objects.get(identifier);
            if (object == null) {
                object = new JsonObject();
                this.objects.put(identifier, object);
                object.add(key, value);
                return;
            }
            object.remove(key);
            object.add(key, value);
        } catch (Exception exception) {
            LOGGER.error("Cannot update value " + key + " of " + wrapper.getIdentifier(), exception);
        }
    }

    private static final class UpdateThread extends Thread {
        private final CommunicationClient client;

        public UpdateThread(CommunicationClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                while (isAlive()) {
                    try {
                        Result<byte[]> result = client.update.awaitUpdate(2000);
                        if (result.isProblematic()) {
                            Problem problem = result.getAsProblematic().getProblem();
                            LOGGER.error("Error in Update thread for topic " + client.update.getTopic() + " on receive",
                                            problem.getError().orElse(new RuntimeException(problem.getCause())));
                            continue;
                        }
                        byte[] bytes = result.get();
                        if (bytes == null) continue;
                        CompletableFuture.runAsync(() -> {
                            try {
                                BackendInputStream input = new BackendInputStream(bytes);
                                String identifier = input.readUTF();
                                String key = input.readUTF();
                                JsonElement value = input.readJsonElement();
                                input.close();

                                JsonObject object = client.objects.get(identifier);
                                if (object == null) return;
                                if (object.get(key) == null) return;
                                object.remove(key);
                                object.add(key, value);
                                this.client.updateConsumer.accept(object);
                            } catch (Exception exception) {
                                LOGGER.error("Cannot update value on topic " + client.update.getTopic());
                            }
                        });
                    } catch (Exception  exception) {
                        LOGGER.error("Unhandled exception in Update thread of topic " + client.update.getTopic());
                    }
                }
            } catch (Exception exception) {
                LOGGER.error("Unhandled exception in Update thread cycle of topic " + client.update.getTopic());
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.request.close();
        this.update.close();
    }
}
