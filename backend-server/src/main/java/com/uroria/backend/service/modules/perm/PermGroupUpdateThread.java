package com.uroria.backend.service.modules.perm;

import com.google.gson.JsonElement;
import com.uroria.backend.impl.communication.broadcast.RabbitUpdateChannel;
import com.uroria.backend.impl.communication.broadcast.UpdateChannel;
import com.uroria.backend.impl.io.BackendInputStream;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;

public final class PermGroupUpdateThread extends Thread {
    private final PermModule module;
    private final UpdateChannel update;
    private final Logger logger;

    public PermGroupUpdateThread(PermModule module) {
        this.module = module;
        this.logger = module.getLogger();
        this.update = new RabbitUpdateChannel(module.getServer().getRabbit(), "permgroup-update");
    }

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
                    String name = input.readUTF();
                    String key = input.readUTF();
                    JsonElement element = input.readJsonElement();
                    input.close();
                    this.module.checkPart(name, key, element);
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