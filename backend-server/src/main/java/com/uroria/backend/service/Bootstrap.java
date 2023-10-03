package com.uroria.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bootstrap");

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String... args) {
        try {
            BackendServer server = new BackendServer();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.shutdown();
                } catch (Exception exception) {
                    LOGGER.error("Unable to run shutdown thread", exception);
                }
            }));
            server.start();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(1);
        }
    }
}
