package com.uroria.backend.service.modules.perm;

import com.google.gson.JsonElement;
import com.uroria.backend.impl.communication.response.RabbitResponseChannel;
import com.uroria.backend.impl.communication.response.Request;
import com.uroria.backend.impl.communication.response.ResponseChannel;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;

public final class PermGroupObjectThread extends Thread {
    private final PermModule module;
    private final ResponseChannel requests;
    private final Logger logger;

    public PermGroupObjectThread(PermModule module) {
        this.module = module;
        this.logger = module.getLogger();
        this.requests = new RabbitResponseChannel(module.getServer().getRabbit(), "permgroup-requests");
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
                    String name = input.readUTF();
                    input.close();
                    BackendOutputStream output = new BackendOutputStream();
                    JsonElement element = this.module.getPart(name, "uuid");
                    if (element.isJsonNull()) output.writeBoolean(false);
                    else {
                        output.writeBoolean(true);
                        output.writeUTF(name);
                    }
                    output.close();
                    request.respondAsync(output.toByteArray());
                } catch (Exception exception) {
                    logger.error("Cannot consume object request", exception);
                }
            } catch (Exception ignored) {}
        }
    }

    public ResponseChannel getResponseChannel() {
        return this.requests;
    }
}
