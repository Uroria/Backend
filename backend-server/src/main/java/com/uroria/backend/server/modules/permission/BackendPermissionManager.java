package com.uroria.backend.server.modules.permission;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.permission.PermissionManager;
import com.uroria.backend.pluginapi.BackendRegistry;
import com.uroria.backend.pluginapi.events.permission.PermissionGroupCreateEvent;
import com.uroria.backend.pluginapi.events.permission.PermissionGroupUpdateEvent;
import com.uroria.backend.pluginapi.events.permission.PermissionHolderRegisterEvent;
import com.uroria.backend.pluginapi.events.permission.PermissionHolderUpdateEvent;
import com.uroria.backend.permission.PermissionGroup;
import com.uroria.backend.permission.PermissionHolder;
import com.uroria.backend.server.Uroria;
import com.uroria.backend.server.events.BackendEventManager;
import com.uroria.backend.server.modules.AbstractManager;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class BackendPermissionManager extends AbstractManager implements PermissionManager {
    private final PulsarClient pulsarClient;
    private final MongoCollection<Document> groups;
    private final MongoCollection<Document> holders;
    private final RedisCommands<String, String> cachedHolders;
    private final BackendEventManager eventManager;
    private BackendHolderResponse holderResponse;
    private BackendGroupResponse groupResponse;
    private BackendHolderUpdate holderUpdate;
    private BackendGroupUpdate groupUpdate;

    public BackendPermissionManager(Logger logger, PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        super(logger, "PermissionModule");
        this.pulsarClient = pulsarClient;
        this.groups = database.getCollection("permission_groups", Document.class);
        this.holders = database.getCollection("permission_holders", Document.class);
        this.cachedHolders = cache.sync();
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
    }

    @Override
    public void enable() {
        try {
            this.holderResponse = new BackendHolderResponse(this.pulsarClient, this);
            this.groupResponse = new BackendGroupResponse(this.pulsarClient, this);
            this.holderUpdate = new BackendHolderUpdate(this.pulsarClient, this);
            this.groupUpdate = new BackendGroupUpdate(this.pulsarClient, this);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
        }
    }

    @Override
    public void disable() {
        try {
            if (this.holderResponse != null) this.holderResponse.close();
            if (this.groupResponse != null) this.groupResponse.close();
            if (this.holderUpdate != null) this.holderUpdate.close();
            if (this.groupUpdate != null) this.groupUpdate.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
        }
    }

    @Override
    public Optional<PermissionGroup> getGroup(@NotNull String name, int timeout) {
        try {
            Document document = this.groups.find(Filters.eq("name", name)).first();
            if (document == null) return Optional.empty();
            return Optional.of(Uroria.getGson().fromJson(document.toJson(), PermissionGroup.class));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return Optional.empty();
        }
    }

    @Override
    public Optional<PermissionHolder> getHolder(@NonNull UUID uuid) {
        return getHolder(uuid, 3000);
    }

    @Override
    public Optional<PermissionGroup> getGroup(@NonNull String name) {
        return getGroup(name, 3000);
    }

    @Override
    public Optional<PermissionHolder> getHolder(@NotNull UUID uuid, int timeout) {
        try {
            PermissionHolder cachedHolder = getCachedHolder(uuid);
            if (cachedHolder != null) return Optional.of(cachedHolder);
            Document savedDocument = this.holders.find(Filters.eq("uuid", uuid.toString())).first();
            if (savedDocument == null) {
                PermissionHolder holder = new PermissionHolder(uuid);
                String json = Uroria.getGson().toJson(holder);
                Document document = Document.parse(json);
                if (this.holders.insertOne(document).wasAcknowledged()) {
                    PermissionHolderRegisterEvent permissionHolderRegisterEvent = new PermissionHolderRegisterEvent(holder);
                    this.eventManager.callEvent(permissionHolderRegisterEvent);
                    this.logger.info("Registered new holder " + holder.getUUID());
                    return Optional.of(holder);
                }
                else return Optional.empty();
            }
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
            return Optional.empty();
        }
    }

    @Override
    public PermissionGroup updateGroup(@NotNull PermissionGroup group) {
        updateDatabase(group);
        this.groupUpdate.update(group);
        return group;
    }

    void updateLocal(PermissionGroup group) {
        updateDatabase(group);
    }

    private void updateDatabase(PermissionGroup group) {
        Document newDocument = Document.parse(Uroria.getGson().toJson(group));
        Document document = this.groups.find(Filters.eq("name", group.getName())).first();
        if (document == null) {
            if (this.groups.insertOne(newDocument).wasAcknowledged()) {
                PermissionGroupCreateEvent permissionGroupCreateEvent = new PermissionGroupCreateEvent(group);
                this.eventManager.callEvent(permissionGroupCreateEvent);
                this.logger.debug("Registered group " + group.getName());
                return;
            }
            this.logger.warn("Could not register group " + group.getName());
            return;
        }
        if (this.groups.replaceOne(Filters.eq("name", group.getName()), newDocument).wasAcknowledged()) {
            PermissionGroupUpdateEvent permissionGroupUpdateEvent = new PermissionGroupUpdateEvent(group);
            this.eventManager.callEvent(permissionGroupUpdateEvent);
            this.logger.debug("Updated group " + group.getName());
        } else this.logger.error("Could not update group " + group.getName());
    }

    @Override
    public PermissionHolder updateHolder(@NotNull PermissionHolder holder) {
        updateDatabase(holder);
        this.holderUpdate.update(holder);
        return holder;
    }

    void updateLocal(PermissionHolder holder) {
        updateDatabase(holder);
    }

    private void updateDatabase(PermissionHolder holder) {
        try {
            this.cachedHolders.del("permission_holder:" + holder.getUUID());
            String json = Uroria.getGson().toJson(holder);
            fromJson(json);
            Document document = Document.parse(json);
            if (this.holders.replaceOne(Filters.eq("uuid", holder.getUUID().toString()), document).wasAcknowledged()) {
                PermissionHolderUpdateEvent permissionHolderUpdateEvent = new PermissionHolderUpdateEvent(holder);
                this.eventManager.callEvent(permissionHolderUpdateEvent);
                this.logger.debug("Updated holder " + holder.getUUID());
                return;
            }
            this.logger.warn("Could not update holder " + holder.getUUID());
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            Uroria.captureException(exception);
        }
    }

    private PermissionHolder fromJson(String json) {
        PermissionHolder holder = Uroria.getGson().fromJson(json, PermissionHolder.class);
        this.cachedHolders.set("permission_holder:" + holder.getUUID(), json, SetArgs.Builder.ex(Duration.ofHours(8)));
        return holder;
    }

    private PermissionHolder getCachedHolder(UUID uuid) {
        String cachedObject = this.cachedHolders.get("permission_holder:" + uuid);
        if (cachedObject == null) return null;
        return Uroria.getGson().fromJson(cachedObject, PermissionHolder.class);
    }
}
