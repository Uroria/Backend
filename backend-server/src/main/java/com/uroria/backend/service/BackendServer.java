package com.uroria.backend.service;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.service.commands.CommandManager;
import com.uroria.backend.service.configuration.MongoConfiguration;
import com.uroria.backend.service.configuration.RedisConfiguration;
import com.uroria.backend.service.console.BackendConsole;
import com.uroria.backend.service.modules.ControllableModule;
import com.uroria.backend.service.modules.clan.ClanModule;
import com.uroria.backend.service.modules.perm.PermModule;
import com.uroria.backend.service.modules.proxy.ProxyModule;
import com.uroria.backend.service.modules.server.ServerModule;
import com.uroria.backend.service.modules.server.group.ServerGroupModule;
import com.uroria.backend.service.modules.user.UserModule;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.resource.ClientResources;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@Getter
public final class BackendServer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Backend");

    private boolean running;
    private final CommandManager commandManager;
    private final BackendConsole console;
    private final ObjectSet<ControllableModule> modules;
    private final Communicator communicator;
    private final MongoClient mongo;
    private final MongoDatabase database;
    private final RedisClient redis;
    private final StatefulRedisConnection<String, String> redisCache;

    public BackendServer() {
        long start = System.currentTimeMillis();
        LOGGER.info("Initializing...");

        this.commandManager = new CommandManager();
        this.console = new BackendConsole(this);
        this.modules = new ObjectArraySet<>();

        this.communicator = new Communicator(LOGGER);

        try {
            this.mongo = connectMongoClient();
            this.database = connectMongoDatabase();
        } catch (Exception exception) {
            LOGGER.error("Unable to initialize mongo connection", exception);
            try {
                shutdownRabbit();
            } catch (Exception ignored) {}
            System.exit(1);
            throw exception;
        }
        try {
            this.redis = connectRedisClient();
            this.redisCache = connectRedisCache();
            this.redisCache.sync().flushdb();
        } catch (Exception exception) {
            LOGGER.error("Unable initialize redis connection", exception);
            try {
                shutdownRabbit();
                shutdownMongo();
                shutdownRedis();
            } catch (Exception ignored) {}
            System.exit(1);
            throw exception;
        }

        try {
            this.modules.add(new UserModule(this));
            this.modules.add(new PermModule(this));
            this.modules.add(new ServerGroupModule(this));
            this.modules.add(new ServerModule(this));
            this.modules.add(new ProxyModule(this));
            this.modules.add(new ClanModule(this));
        } catch (Exception exception) {
            LOGGER.error("Unable to initialize some module", exception);
            try {
                shutdownRabbit();
                shutdownMongo();
                shutdownRedis();
            } catch (Exception anotherException) {
                LOGGER.error("Unable to shutdown connections on error", anotherException);
            }
            System.exit(1);
            throw exception;
        }

        LOGGER.info("Initialized in " + (System.currentTimeMillis() - start) + "ms");
    }

    private MongoClient connectMongoClient() {
        MongoClientSettings.Builder settings = MongoClientSettings.builder();
        String url = MongoConfiguration.getMongoUrl();
        settings.applyConnectionString(new ConnectionString(url));
        LOGGER.info("Connecting to MongoDB with url " + url);
        return MongoClients.create(settings.build());
    }

    private MongoDatabase connectMongoDatabase() {
        String name = MongoConfiguration.getMongoDatabase();
        LOGGER.info("Connecting to MongoDB database " + name);
        return this.mongo.getDatabase(name);
    }

    private RedisClient connectRedisClient() {
        ClientResources.Builder builder = ClientResources.builder();
        String url = RedisConfiguration.getRedisUrl();
        LOGGER.info("Connecting to Redis with url " + url);
        return RedisClient.create(builder.build(), url);
    }

    private StatefulRedisConnection<String, String> connectRedisCache() {
        LOGGER.info("Connecting to Redis cache");
        return this.redis.connect();
    }

    public void start() {
        if (this.running) return;
        this.running = true;

        long start = System.currentTimeMillis();
        LOGGER.info("Starting...");

        for (ControllableModule module : this.modules) {
            try {
                LOGGER.info("Enabling module " + module.getModuleName());
                module.enable();
            } catch (Exception exception) {
                LOGGER.error("Unable to enable module " + module.getModuleName(), exception);
            }
        }

        registerCommands();
        setupConsole();

        LOGGER.info("Started in " + (System.currentTimeMillis() - start) + "ms");
    }

    private void registerCommands() {
        LOGGER.info("Registering commands");
    }

    private void setupConsole() {
        LOGGER.info("Setting up console");
        this.console.setupStreams();
        CompletableFuture.runAsync(this.console::start);
    }

    public void shutdown() throws Exception {
        if (!this.running) return;
        this.running = false;

        long start = System.currentTimeMillis();
        LOGGER.info("Shutting down...");

        for (ControllableModule module : this.modules) {
            try {
                LOGGER.info("Disabling module " + module.getModuleName());
                module.disable();
            } catch (Exception exception) {
                LOGGER.error("Unable to disable module " + module.getModuleName(), exception);
            }
        }

        shutdownRabbit();
        shutdownRedis();
        shutdownMongo();

        LOGGER.info("Shutdown in " + (System.currentTimeMillis() - start) + "ms");
    }

    private void shutdownRabbit() {
        LOGGER.info("Shutting down Rabbit connection");
        this.communicator.close();
    }

    private void shutdownMongo() {
        LOGGER.info("Shutting down MongoDB connection");
        this.mongo.close();
    }

    private void shutdownRedis() {
        LOGGER.info("Shutting down Redis connection");
        this.redisCache.close();
        this.redis.shutdown();
    }

    public void explicitShutdown() {
        try {
            shutdown();
        } catch (Exception exception) {
            LOGGER.error("Terminating with exit code 1", exception);
            System.exit(1);
            return;
        }
        LOGGER.info("Terminating with exit code 0");
        System.exit(0);
    }
}
