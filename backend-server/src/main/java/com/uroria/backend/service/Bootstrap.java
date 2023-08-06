package com.uroria.backend.service;

public final class Bootstrap {

    public static void main(String... args) {
        try {
            BackendServer server = new BackendServer();
            Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
            server.start();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(1);
        }
    }
}
