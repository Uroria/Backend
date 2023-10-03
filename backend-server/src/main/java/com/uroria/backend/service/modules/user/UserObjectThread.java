package com.uroria.backend.service.modules.user;

import com.uroria.backend.impl.communication.response.RabbitResponseChannel;
import com.uroria.backend.impl.communication.response.Request;
import com.uroria.backend.impl.communication.response.ResponseChannel;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;

import java.util.UUID;

public final class UserObjectThread extends Thread {
    private final UserModule module;
    private final ResponseChannel requests;
    private final Logger logger;

    public UserObjectThread(UserModule module) {
        this.module = module;
        this.logger = module.getLogger();
        this.requests = new RabbitResponseChannel(module.getServer().getRabbit(), "user-requests");
    }

    @Override
    public void run() {
        while (module.getServer().isRunning()) {
            try {
                Result<Request> result = requests.awaitRequest(5000);
                if (result.isProblematic()) {
                    Problem problem = result.getAsProblematic().getProblem();
                    this.logger.warn("Unable to receive request", problem.getError().orElse(null));
                    return;
                }
                Request request = result.get();
                if (request == null) continue;
                try (BackendInputStream input = new BackendInputStream(request.getData())) {
                    byte type = input.readByte();
                    String object = input.readUTF();
                    input.close();
                    InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream();
                    if (type == 1) {
                        UUID uuid = module.getUUID(object);
                        if (uuid == null) output.writeBoolean(false);
                        else {
                            output.writeBoolean(true);
                            output.writeUTF(uuid.toString());
                        }
                        output.close();
                        request.respondAsync(output.toByteArray());
                        continue;
                    }
                    output.writeUTF(object);
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
