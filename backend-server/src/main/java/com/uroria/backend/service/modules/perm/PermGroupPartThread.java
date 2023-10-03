package com.uroria.backend.service.modules.perm;

import com.uroria.backend.impl.communication.response.RabbitResponseChannel;
import com.uroria.backend.impl.communication.response.Request;
import com.uroria.backend.impl.communication.response.ResponseChannel;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;

public final class PermGroupPartThread extends Thread {
    private final PermModule module;
    private final ResponseChannel requests;
    private final Logger logger;

    public PermGroupPartThread(PermModule module) {
        this.module = module;
        this.logger = module.getLogger();
        this.requests = new RabbitResponseChannel(module.getServer().getRabbit(), "permgroup-request");
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
                try (BackendInputStream input = new BackendInputStream(request.getData())) {
                    String name = input.readUTF();
                    String key = input.readUTF();
                    input.close();
                    BackendOutputStream output = new BackendOutputStream();
                    output.writeJsonElement(module.getPart(name, key));
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
