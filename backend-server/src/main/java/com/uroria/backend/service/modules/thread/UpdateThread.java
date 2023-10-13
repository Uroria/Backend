package com.uroria.backend.service.modules.thread;

import com.uroria.backend.impl.communication.broadcast.RabbitUpdateChannel;
import com.uroria.backend.impl.communication.broadcast.UpdateChannel;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.backend.service.modules.BackendModule;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;

public abstract class UpdateThread extends Thread {
    private final BackendModule module;
    protected final UpdateChannel update;
    protected final Logger logger;

    public UpdateThread(BackendModule module, String topic) {
        this.module = module;
        this.logger = module.getLogger();
        this.update = new RabbitUpdateChannel(module.getServer().getRabbit(), topic);
    }

    protected abstract void update(BackendInputStream input) throws Exception;

    @Override
    public void run() {
        while (module.getServer().isRunning()) {
            try {
                Result<byte[]> result = update.awaitUpdate(5000);
                if (result.isProblematic()) {
                    Problem problem = result.getAsProblematic().getProblem();
                    this.logger.warn("Unable to receive request", problem.getError().orElse(null));
                    return;
                }
                byte[] bytes = result.get();
                if (bytes == null) continue;
                try (BackendInputStream input = new BackendInputStream(bytes)) {
                    update(input);
                } catch (Exception exception) {
                    logger.error("Cannot consume object request", exception);
                }
            } catch (Exception ignored) {}
        }
    }

    public UpdateChannel getUpdateChannel() {
        return this.update;
    }
}
