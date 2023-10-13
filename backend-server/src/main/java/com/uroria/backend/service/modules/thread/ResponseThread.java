package com.uroria.backend.service.modules.thread;

import com.uroria.backend.impl.communication.response.RabbitResponseChannel;
import com.uroria.backend.impl.communication.response.Request;
import com.uroria.backend.impl.communication.response.ResponseChannel;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.impl.io.BackendOutputStream;
import com.uroria.backend.service.modules.BackendModule;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;

public abstract class ResponseThread extends Thread {
    private final BackendModule module;
    protected final ResponseChannel requests;
    protected final Logger logger;

    public ResponseThread(BackendModule module, String topic) {
        this.module = module;
        this.logger = module.getLogger();
        this.requests = new RabbitResponseChannel(module.getServer().getRabbit(), topic);
    }

    protected abstract void request(BackendInputStream input, BackendOutputStream output) throws Exception;

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
                    BackendOutputStream output = new BackendOutputStream();
                    request(input, output);
                    input.close();
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
