package com.uroria.backend.service;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.SslContextFactory;
import com.uroria.backend.impl.configurations.RabbitConfiguration;
import com.uroria.backend.service.commands.CommandManager;
import com.uroria.backend.service.configuration.MongoConfiguration;
import com.uroria.backend.service.configuration.RedisConfiguration;
import com.uroria.backend.service.console.BackendConsole;
import com.uroria.backend.service.modules.BackendModule;
import com.uroria.backend.service.modules.perm.PermModule;
import com.uroria.backend.service.modules.user.UserModule;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.resource.ClientResources;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.CompletableFuture;

public final class BackendServer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Backend");

    @Getter private boolean running;
    @Getter private final CommandManager commandManager;
    @Getter private final BackendConsole console;
    @Getter private final ObjectSet<BackendModule> modules;
    @Getter private final Connection rabbit;
    @Getter private final MongoClient mongo;
    @Getter private final MongoDatabase database;
    @Getter private final RedisClient redis;
    @Getter private final StatefulRedisConnection<String, String> redisCache;

    public BackendServer() {
        long start = System.currentTimeMillis();
        LOGGER.info("Initializing...");

        this.commandManager = new CommandManager();
        this.console = new BackendConsole(this);
        this.modules = new ObjectArraySet<>();

        this.rabbit = connectRabbitMq();
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
        }

        LOGGER.info("Initialized in " + (System.currentTimeMillis() - start) + "ms");
    }

    private Connection connectRabbitMq() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(RabbitConfiguration.getUsername());
            factory.setPassword(RabbitConfiguration.getPassword());
            factory.setVirtualHost(RabbitConfiguration.getVirtualHost());
            factory.setHost(RabbitConfiguration.getHostname());
            factory.setPort(RabbitConfiguration.getPort());
            if (RabbitConfiguration.isSslEnabled()) {
                KeyStore ks = KeyStore.getInstance("PKCS12");
                ks.load(new FileInputStream(RabbitConfiguration.getSslCertPath()), RabbitConfiguration.getSslCertPassword().toCharArray());

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, RabbitConfiguration.getSslCertPassword().toCharArray());

                KeyStore tks = KeyStore.getInstance("JKS");
                tks.load(new FileInputStream(RabbitConfiguration.getSslKeyStorePath()), RabbitConfiguration.getSslKeyStorePassword().toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(tks);

                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

                factory.useSslProtocol(sslContext);
                factory.enableHostnameVerification();
            }
            return factory.newConnection();
        } catch (Exception exception) {
            LOGGER.error("Cannot connect to RabbitMQ", exception);
            System.exit(1);
            return null;
        }
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

        for (BackendModule module : this.modules) {
            module.start();
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

        for (BackendModule module : this.modules) {
            module.shutdown();
        }

        shutdownRabbit();
        shutdownRedis();
        shutdownMongo();

        LOGGER.info("Shutdown in " + (System.currentTimeMillis() - start) + "ms");
    }

    private void shutdownRabbit() throws IOException {
        LOGGER.info("Shutting down Rabbit connection");
        this.rabbit.close();
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
