package com.uroria.backend.impl.clan;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uroria.backend.Backend;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.wrapper.Wrapper;
import com.uroria.backend.user.User;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ClanWrapper extends Wrapper implements Clan {
    private final CommunicationWrapper object;
    private final String name;
    private boolean deleted;

    public ClanWrapper(@NonNull CommunicationClient client, @NonNull String name) {
        this.object = new CommunicationWrapper(name, client);
        this.name = name;
    }

    @Override
    public void refresh() {

    }

    @Override
    public JsonObject getObject() {
        return this.object.getObject();
    }

    @Override
    public CommunicationWrapper getObjectWrapper() {
        return this.object;
    }

    @Override
    public String getIdentifierKey() {
        return "name";
    }

    @Override
    public String getStringIdentifier() {
        return this.name;
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.object.set("deleted", true);
        this.object.remove("group." + name);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        Result<JsonElement> result = object.get("deleted");
        JsonElement element = result.get();
        if (element == null) return false;
        boolean deleted = element.getAsBoolean();
        if (deleted) this.deleted = true;
        return deleted;
    }

    @Override
    public String getTag() {
        return getString("tag", this.name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setTag(@NonNull String tag) {
        this.object.set("tag", tag);
    }

    @Override
    public long getFoundingDate() {
        return getLong("foundingDate", 0L);
    }

    @Override
    public boolean hasMember(@NonNull User user) {
        return getUUIDArray("members").contains(user.getUniqueId());
    }

    @Override
    public void addMember(@NonNull User user) {
        List<UUID> members = getUUIDArray("members");
        members.add(user.getUniqueId());
        setUUIDArray(members, "members");
        user.joinClan(this);
    }

    @Override
    public void addOperator(@NonNull User user) {
        List<UUID> operators = getUUIDArray("operators");
        operators.add(user.getUniqueId());
        setUUIDArray(operators, "members");
    }

    public void addOperator(UUID uuid) {
        List<UUID> operators = getUUIDArray("operators");
        operators.add(uuid);
        setUUIDArray(operators, "members");
    }

    @Override
    public void addModerator(@NonNull User user) {
        List<UUID> moderators = getUUIDArray("moderators");
        moderators.add(user.getUniqueId());
        setUUIDArray(moderators, "members");
    }

    @Override
    public void removeMember(User user) {
        removeMember(user.getUniqueId());
    }

    @Override
    public void removeModerator(User user) {
        removeModerator(user.getUniqueId());
    }

    @Override
    public void removeOperator(User user) {
        removeOperator(user.getUniqueId());
    }

    @Override
    public void removeMember(UUID uuid) {
        removeOperator(uuid);
        removeModerator(uuid);
        Backend.getUser(uuid).ifPresent(User::leaveClan);
        List<UUID> members = getUUIDArray("members");
        members.remove(uuid);
        setUUIDArray(members, "members");
    }

    @Override
    public void removeModerator(UUID uuid) {
        List<UUID> members = getUUIDArray("moderators");
        members.remove(uuid);
        setUUIDArray(members, "moderators");
    }

    @Override
    public void removeOperator(UUID uuid) {
        List<UUID> members = getUUIDArray("operators");
        members.remove(uuid);
        setUUIDArray(members, "operators");
    }

    @Override
    public Collection<User> getOperators() {
        return getUUIDArray("operators").stream()
                .map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Collection<User> getMembers() {
        return getUUIDArray("members").stream()
                .map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Collection<User> getModerators() {
        return getUUIDArray("moderators").stream()
                .map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    private List<UUID> getUUIDArray(String key) {
        Result<JsonElement> result = this.object.get(key);
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray array = element.getAsJsonArray();
        return array.asList().stream()
                .map(el -> uuidFromString(el.getAsString()))
                .filter(Objects::nonNull)
                .toList();
    }

    private void setUUIDArray(List<UUID> list, String key) {
        Result<JsonElement> result = this.object.get(key);
        JsonArray array = new JsonArray();
        list.forEach(uuid -> array.add(uuid.toString()));
        this.object.set(key, array);
    }

    private UUID uuidFromString(String string) {
        try {
            return UUID.fromString(string);
        } catch (Exception exception) {
            return null;
        }
    }
}
