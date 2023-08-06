package com.uroria.backend.service.commands.predefined;

import com.uroria.backend.service.commands.Command;
import org.apache.pulsar.shade.com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class HelpCommand extends Command {
    private static final Logger logger = LoggerFactory.getLogger("Help");

    @Override
    protected void run(String... args) {
        StringBuilder builder = new StringBuilder();
        builder.append("Commands:\n");
        builder.append("stop - Shutdown the server\n");
        logger.info(builder.toString());
    }

    @Override
    protected List<String> tabComplete(String... args) {
        return ImmutableList.of();
    }
}
