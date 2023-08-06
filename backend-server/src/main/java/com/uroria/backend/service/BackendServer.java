package com.uroria.backend.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.uroria.backend.impl.configuration.BackendConfiguration;
import com.uroria.backend.service.commands.CommandManager;
import com.uroria.backend.service.commands.predefined.HelpCommand;
import com.uroria.backend.service.commands.predefined.StopCommand;
import com.uroria.backend.service.console.BackendConsole;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.Getter;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class BackendServer {
    private static @Getter final Logger logger = LoggerFactory.getLogger("Backend");

    private @Getter boolean running;
    private @Getter final CommandManager commandManager;
    private @Getter final BackendConsole console;
    private @Getter final PulsarClient pulsarClient;
    private @Getter final MongoClient mongoClient;
    private @Getter final MongoDatabase database;
    private @Getter final RedisClient redisClient;
    private @Getter final StatefulRedisConnection<String, String> redisConnection;
    private @Getter final BackendImpl backend;

    public BackendServer() {
        logger.info("Initializing...");
        this.commandManager = new CommandManager();
        this.console = new BackendConsole(this);

        logger.info("Connecting to pulsar instance...");
        this.pulsarClient = buildPulsarClient(BackendConfiguration.getPulsarURL());

        logger.info("Connecting to mongo instance...");
        this.mongoClient = MongoClients.create(BackendConfiguration.getString("mongo.url"));
        this.database = this.mongoClient.getDatabase(BackendConfiguration.getString("mongo.database"));

        logger.info("Connecting to redis instance...");
        this.redisClient = RedisClient.create(BackendConfiguration.getString("redis.url"));
        this.redisConnection = this.redisClient.connect();

        logger.info("Initializing backend...");
        this.backend = new BackendImpl(this.pulsarClient, database, redisConnection);
    }

    public void start() {
        logger.info("Starting...");
        long start = System.currentTimeMillis();
        this.running = true;
        this.console.setupStreams();
        CompletableFuture.runAsync(this.console::start);
        this.commandManager.register(new HelpCommand(), "help");
        this.commandManager.register(new StopCommand(this), "stop");
        try {
            this.backend.start();
        } catch (Exception exception) {
            logger.error("Cannot start backend implementation", exception);
        }
        logger.info("Started in " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void explicitShutdown() {
        shutdown();
        System.exit(0);
    }

    public void shutdown() {
        if (!this.running) return;
        this.running = false;
        logger.info("Shutting down...");
        try {
            this.backend.shutdown();
        } catch (Exception exception) {
            logger.error("Unhandled exception while shutting down backend implementation", exception);
        }

        logger.info("Closing redis connection...");
        this.redisConnection.close();

        logger.info("Closing redis client...");
        this.redisClient.close();

        logger.info("Closing mongo client...");
        this.mongoClient.close();

        logger.info("Finished shutting down. Good bye! :D");
    }

    private PulsarClient buildPulsarClient(String url) {
        try {
            return PulsarClient.builder()
                    .serviceUrl(url)
                    .statsInterval(10, TimeUnit.MINUTES).build();
        } catch (Exception exception) {
            logger.error("Cannot build pulsar instance");
            return null;
        }
    }
}
