package com.uroria.backend.service.modules.user;

import com.uroria.backend.impl.communication.response.RabbitResponseChannel;
import com.uroria.backend.impl.communication.response.Request;
import com.uroria.backend.impl.communication.response.ResponseChannel;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;

import java.util.UUID;

public final class UserPartThread extends Thread {
    private final UserModule module;
    private final ResponseChannel requests;
    private final Logger logger;

    public UserPartThread(UserModule module) {
        this.module = module;
        this.logger = module.getLogger();
        this.requests = new RabbitResponseChannel(module.getServer().getRabbit(), "user-request");
    }

    @Override
    public void run() {
        while (module.getServer().isRunning()) {
            try {
                Result<Request> result = requests.awaitRequest(5000);
                if (result.isProblematic()) {
                    Problem problem = result.getAsProblematic().getProblem();
                    this.logger.warn("Unable to receive request", problem.getError().orElse(null));
                    continue;
                }
                Request request = result.get();
                if (request == null) continue;
                try (InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(request.getData())) {
                    UUID uuid = UUID.fromString(input.readUTF());
                    String key = input.readUTF();
                    input.close();
                    BackendOutputStream output = new BackendOutputStream();
                    output.writeJsonElement(module.getPart(uuid, key));
                    output.close();
                    request.respondAsync(output.toByteArray());
                } catch (Exception exception) {
                    logger.error("Cannot consume object request", exception);
                }
            } catch (Exception ignored) {}
        }
    }

    public ResponseChannel getResponseChannel() {
        return requests;
    }
}
