package com.uroria.backend.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.uroria.backend.api.BackendRegistry;
import com.uroria.backend.api.Server;
import com.uroria.backend.api.events.EventManager;
import com.uroria.backend.api.modules.PartyManager;
import com.uroria.backend.api.modules.PermissionManager;
import com.uroria.backend.api.modules.PlayerManager;
import com.uroria.backend.api.modules.StatsManager;
import com.uroria.backend.api.plugins.PluginManager;
import com.uroria.backend.api.scheduler.Scheduler;
import com.uroria.backend.server.events.BackendEventManager;
import com.uroria.backend.server.modules.party.BackendPartyManager;
import com.uroria.backend.server.modules.permission.BackendPermissionManager;
import com.uroria.backend.server.modules.player.BackendPlayerManager;
import com.uroria.backend.server.modules.stats.BackendStatsManager;
import com.uroria.backend.server.plugins.BackendPluginManager;
import com.uroria.backend.server.scheduler.BackendScheduler;
import de.leonhard.storage.Json;
import de.leonhard.storage.internal.settings.ReloadSettings;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.sentry.Sentry;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Uroria implements Server {
    private static final Gson GSON;
    private static final Logger LOGGER = LoggerFactory.getLogger(Uroria.class);
    private static final Json CONFIG;
    private static boolean sentry;

    static {
        GSON = new GsonBuilder().disableHtmlEscaping().create();
        CONFIG = new Json("config.json", "./", Uroria.class.getClassLoader().getResourceAsStream("config.json"), ReloadSettings.MANUALLY);
    }

    public static void main(String... args) {
        Uroria server = new Uroria();
        Runtime.getRuntime().addShutdownHook(new Thread(server::terminate));
        server.start();
    }

    private final PulsarClient pulsarClient;
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> redisConnection;
    private final BackendScheduler scheduler;
    private final BackendEventManager eventManager;
    private final BackendPluginManager pluginManager;

    private final BackendPlayerManager playerManager;
    private final BackendStatsManager statsManager;
    private final BackendPermissionManager permissionManager;
    private final BackendPartyManager partyManager;

    private Uroria() {
        BackendRegistry.register(this);

        sentry = CONFIG.getOrSetDefault("sentry.enabled", false);

        if (sentry) {
            LOGGER.info("Initializing sentry");
            Sentry.init(options -> {
                options.setDsn(CONFIG.getString("sentry.dsn"));
                options.setTracesSampleRate(1.0);
            });
            Sentry.configureScope(scope -> {
                scope.setTag("System", "Backend");
            });
        }

        this.eventManager = new BackendEventManager();

        LOGGER.info("Connecting to pulsar instance");
        this.pulsarClient = buildPulsarClient(CONFIG.getOrSetDefault("pulsar.url", "pulsar://pls.api.uroria.net:6650"));

        LOGGER.info("Connecting to mongo instance");
        this.mongoClient = MongoClients.create(CONFIG.getString("mongo.url"));
        this.database = this.mongoClient.getDatabase("backend");

        LOGGER.info("Connecting to redis instance");
        this.redisClient = RedisClient.create(CONFIG.getString("redis.url"));
        this.redisConnection = this.redisClient.connect();

        this.scheduler = new BackendScheduler();
        this.pluginManager = new BackendPluginManager(this);

        this.playerManager = new BackendPlayerManager(LOGGER, this.pulsarClient, this.database, this.redisConnection);
        this.statsManager = new BackendStatsManager(LOGGER, this.pulsarClient, this.database, this.redisConnection);
        this.permissionManager = new BackendPermissionManager(LOGGER, this.pulsarClient, this.database, this.redisConnection);
        this.partyManager = new BackendPartyManager(LOGGER, this.pulsarClient, this.redisConnection);
    }

    private void start() {
        this.playerManager.start();
        this.statsManager.start();
        this.permissionManager.start();

        LOGGER.info("Starting plugins");
        this.pluginManager.startPlugins();
    }

    private void terminate() {
        LOGGER.info("Stopping plugins...");
        this.pluginManager.stopPlugins();

        this.playerManager.shutdown();
        this.statsManager.shutdown();
        this.permissionManager.shutdown();

        try {
            if (this.pulsarClient != null && !this.pulsarClient.isClosed()) {
                LOGGER.info("Shutting down pulsar...");
                this.pulsarClient.shutdown();
            }
        } catch (PulsarClientException exception) {
            LOGGER.error("Error while trying to shutdown pulsar", exception);
        }
        if (this.redisConnection != null) {
            LOGGER.info("Closing Redis connection");
            this.redisConnection.close();
        }
        if (this.redisClient != null) {
            LOGGER.info("Closing Redis client");
            this.redisClient.close();
        }
        if (this.mongoClient != null) {
            LOGGER.info("Closing mongo connection...");
            this.mongoClient.close();
        }
        LOGGER.info("Finished shutting down. Goodbye!");
    }

    @Override
    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    @Override
    public StatsManager getStatsManager() {
        return this.statsManager;
    }

    @Override
    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    @Override
    public PartyManager getPartyManager() {
        return this.partyManager;
    }

    @Override
    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public PulsarClient getPulsarClient() {
        return pulsarClient;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public RedisClient getRedisClient() {
        return redisClient;
    }

    public StatefulRedisConnection<String, String> getRedisConnection() {
        return redisConnection;
    }

    public boolean isSentry() {
        return sentry;
    }

    private PulsarClient buildPulsarClient(String url) {
        try {
            return PulsarClient.builder().serviceUrl(url).build();
        } catch (PulsarClientException exception) {
            LOGGER.error("Cannot connect to Pulsar instance", exception);
            return null;
        }
    }

    public static void captureException(Throwable throwable) {
        if (!sentry) return;
        Sentry.captureException(throwable, scope -> {
            scope.setTag("Service", "Backend");

            Thread thread = Thread.currentThread();
            scope.setContexts("Thread.Name", thread.getName());
            scope.setContexts("Thread.Alive", thread.isAlive());
        });
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Gson getGson() {
        return GSON;
    }
}
