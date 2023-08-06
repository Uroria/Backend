package com.uroria.backend.service.console;

import com.uroria.backend.service.BackendServer;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

public final class BackendConsole extends SimpleTerminalConsole {
    private static final Logger logger = LogManager.getLogger(BackendConsole.class);
    private final BackendServer server;

    public BackendConsole(BackendServer server) {
        this.server = server;
    }

    public void setupStreams() {
        System.setOut(IoBuilder.forLogger(logger).setLevel(Level.INFO).buildPrintStream());
        System.setErr(IoBuilder.forLogger(logger).setLevel(Level.ERROR).buildPrintStream());
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        return super.buildReader(builder.appName("Backend")
                .completer((reader, parsedLine, list) -> {

                }));
    }

    @Override
    protected boolean isRunning() {
        return this.server.isRunning();
    }

    @Override
    protected void runCommand(String command) {
        if (command.isEmpty()) return;
        this.server.getCommandManager().runCommand(command);
    }

    @Override
    public void shutdown() {
        this.server.explicitShutdown();
    }
}
