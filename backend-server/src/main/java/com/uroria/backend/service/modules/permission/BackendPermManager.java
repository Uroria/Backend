package com.uroria.backend.service.modules.permission;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.PermHolder;
import com.uroria.backend.permission.PermManager;
import com.uroria.backend.service.modules.AbstractManager;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bson.Document;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class BackendPermManager extends AbstractManager implements PermManager {
    private final MongoCollection<Document> groups;
    private final MongoCollection<Document> holders;
    private final RedisCommands<String, String> cache;
    private HolderUpdate holderUpdate;
    private HolderResponse holderResponse;
    private GroupUpdate groupUpdate;
    private GroupResponse groupResponse;

    public BackendPermManager(PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> cache) {
        super(pulsarClient, "PermModule");
        this.groups = database.getCollection("perm_groups", Document.class);
        this.holders = database.getCollection("perm_holders", Document.class);
        this.cache = cache.sync();
    }

    @Override
    protected void enable() throws PulsarClientException {
        this.holderUpdate = new HolderUpdate(this.pulsarClient, this);
        this.holderResponse = new HolderResponse(this.pulsarClient, this);
        this.groupUpdate = new GroupUpdate(this.pulsarClient, this);
        this.groupResponse = new GroupResponse(this.pulsarClient, this);
    }

    @Override
    protected void disable() throws PulsarClientException {
        if (this.holderUpdate != null) this.holderUpdate.close();
        if (this.holderResponse != null) this.holderResponse.close();
        if (this.groupUpdate != null) this.groupUpdate.close();
        if (this.groupResponse != null) this.groupResponse.close();
    }

    @Override
    public Optional<PermHolder> getHolder(UUID uuid, int timeout) {
        try {
            PermHolder cachedHolder = getCachedHolder(uuid);
            if (cachedHolder != null) return Optional.of(cachedHolder);
            Document savedDocument = this.holders.find(Filters.eq("uuid", uuid.toString())).first();
            if (savedDocument == null) {
                PermHolder holder = new PermHolder(uuid);
                holder.addGroup(getDefaultGroup());
                String json = gson.toJson(holder);
                Document document = Document.parse(json);
                if (this.holders.insertOne(document).wasAcknowledged()) {
                    this.logger.debug("Inserted " + holder);
                    return Optional.of(holder);
                }
                return Optional.empty();
            }
            return Optional.of(fromJson(savedDocument.toJson()));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            return Optional.empty();
        }
    }

    private PermGroup getDefaultGroup() {
        PermGroup group = getGroup("default").orElse(null);
        if (group == null) {
            group = new PermGroup("default");
            group.update();
        }
        return group;
    }

    @Override
    public Optional<PermGroup> getGroup(String name, int timeout) {
        try {
            Document document = this.groups.find(Filters.eq("name", name)).first();
            if (document == null) return Optional.empty();
            return Optional.of(gson.fromJson(document.toJson(), PermGroup.class));
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            return Optional.empty();
        }
    }

    @Override
    public void updateHolder(@NonNull PermHolder holder) {
        updateDatabase(holder);
        this.holderUpdate.update(holder);
    }

    @Override
    public void updateGroup(@NonNull PermGroup group) {
        updateDatabase(group);
        this.groupUpdate.update(group);
    }

    void updateDatabase(@NonNull PermGroup group) {
        if (group.isDeleted()) {
            if (this.groups.deleteOne(Filters.eq("name", group.getName())).wasAcknowledged()) {
                logger.debug("Deleted " + group);
                return;
            }
            logger.warn("Unable to delete " + group);
            return;
        }
        Document newDocument = Document.parse(gson.toJson(group));
        Document document = this.groups.find(Filters.eq("name", group.getName())).first();
        if (document == null) {
            if (this.groups.insertOne(newDocument).wasAcknowledged()) {
                this.logger.debug("Inserted " + group);
                return;
            }
            this.logger.warn("Unable to insert " + group);
        }
        if (groups.replaceOne(Filters.eq("name", group.getName()), newDocument).wasAcknowledged()) {
            this.logger.debug("Replaced " + group);
            return;
        }
        this.logger.warn("Unable to replace " + group);
    }

    void updateDatabase(@NonNull PermHolder holder) {
        try {
            this.cache.del("permholder:" + holder.getUUID());
            if (holder.isDeleted()) {
                if (this.holders.deleteOne(Filters.eq("uuid", holder.getUUID().toString())).wasAcknowledged()) {
                    logger.debug("Deleted " + holder);
                    return;
                }
                logger.warn("Deleted " + holder);
                return;
            }
            String json = gson.toJson(holder);
            fromJson(json);
            Document document = Document.parse(json);
            if (this.holders.replaceOne(Filters.eq("uuid", holder.getUUID().toString()), document).wasAcknowledged()) {
                this.logger.debug("Replaced " + holder);
                return;
            }
            this.logger.warn("Unable to replace " + holder);
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
        }
    }

    private PermHolder fromJson(String json) {
        PermHolder holder = gson.fromJson(json, PermHolder.class);
        this.cache.set("permholder:" + holder.getUUID(), json, lifespan(Duration.ofHours(8)));
        return holder;
    }

    private PermHolder getCachedHolder(UUID uuid) {
        String cachedObject = this.cache.get("permholder:" + uuid);
        if (cachedObject == null) return null;
        return gson.fromJson(cachedObject, PermHolder.class);
    }
}
