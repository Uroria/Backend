package com.uroria.backend.impl.pulsar;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import lombok.NonNull;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;

import java.util.Random;
import java.util.function.Consumer;

public final class PulsarRequestChannel extends PulsarChannel {
    private static long lastId;

    public PulsarRequestChannel(@NonNull PulsarClient client, CryptoKeyReader cryptoKeyReader, @NonNull String name, @NonNull String topic) {
        super(client, cryptoKeyReader, name, topic);
    }

    public Result<JsonElement> request(String key, long timeout) {
        try (InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream()) {
            output.writeUTF(key);
            output.close();
            Result<MessageId> result = send(output.toByteArray());
            if (result instanceof Result.Problematic<MessageId> problem) {
                return Result.problem(problem.getProblem());
            }
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }

        long start = System.currentTimeMillis();
        try {
            JsonElement value = null;
            while (true) {
                if ((System.currentTimeMillis() - start) > timeout) break;
                Result<Message<byte[]>> result = receive();
                if (result instanceof Result.Problematic<Message<byte[]>> problem) {
                    return Result.problem(problem.getProblem());
                }

                Message<byte[]> message = result.get();
                if (message == null) continue;

                try (InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(message.getData())) {
                    String aKey = input.readUTF();
                    if (!key.equals(aKey)) {
                        nAck(message);
                        continue;
                    }
                    ack(message);
                    String valueString = input.readUTF();
                    value = JsonParser.parseString(valueString);
                    break;
                } catch (Exception exception) {
                    return Result.problem(Problem.error(exception));
                }
            }
            return Result.of(value);
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    public Result<InsaneByteArrayInputStream> request(@NonNull Consumer<InsaneByteArrayOutputStream> key, long timeout) {
        final long id = System.currentTimeMillis() - new Random().nextLong();
        if (id == lastId) {
            return request(key, timeout);
        }
        lastId = id;
        try (InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream()) {
            output.writeLong(id);
            key.accept(output);
            output.close();
            Result<MessageId> result = send(output.toByteArray());
            if (result instanceof Result.Problematic<MessageId> problem) {
                return Result.problem(problem.getProblem());
            }
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }

        long start = System.currentTimeMillis();
        try {
            InsaneByteArrayInputStream value = null;
            while (true) {
                if ((System.currentTimeMillis() - start) > timeout) break;
                Result<Message<byte[]>> result = receive();

                if (result instanceof Result.Problematic<Message<byte[]>> problem) {
                    return Result.problem(problem.getProblem());
                }

                Message<byte[]> message = result.get();
                if (message == null) continue;

                try (InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(message.getData())) {
                    long idResponse = input.readLong();
                    if (idResponse != id) {
                        nAck(message);
                        continue;
                    }
                    value = input;
                    break;
                } catch (Exception exception) {
                    return Result.problem(Problem.error(exception));
                }
            }
            return Result.of(value);
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }
}
