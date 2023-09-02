package com.uroria.backend.friend;

import com.uroria.backend.Backend;
import com.uroria.backend.BackendObject;
import com.uroria.base.utils.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class FriendHolder extends BackendObject<FriendHolder> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private final ObjectList<FriendPair> friends;
    private final ObjectList<UUID> friendRequests;
    private final ObjectList<UUID> sentFriendRequests;

    public FriendHolder(@NonNull UUID uuid) {
        this.uuid = uuid;
        this.friends = new ObjectArrayList<>();
        this.friendRequests = new ObjectArrayList<>();
        this.sentFriendRequests = new ObjectArrayList<>();
    }

    public void addFriendRequest(FriendHolder friend) {
        if (deleted) return;
        this.friendRequests.add(friend.uuid);
        friend.sentFriendRequests.add(this.uuid);
    }

    public void removeFriendRequest(FriendHolder friend) {
        if (deleted) return;
        this.friendRequests.remove(friend.uuid);
        friend.sentFriendRequests.remove(this.uuid);
    }

    public void addFriend(@NonNull FriendHolder friend, long friendShipDate) {
        if (deleted) return;
        if (this.friends.stream().anyMatch(pair -> pair.friend().equals(friend.getUUID()))) return;
        this.friendRequests.remove(friend.uuid);
        friend.sentFriendRequests.remove(this.uuid);
        this.friends.add(new FriendPair(friend.getUUID(), friendShipDate));
        friend.addFriend(this, friendShipDate);
        friend.update();
        update();
    }

    public void addFriend(@NonNull FriendHolder friend) {
        addFriend(friend, System.currentTimeMillis());
    }

    public void removeFriend(FriendHolder friend) {
        if (friend == null) return;
        if (deleted) return;
        if (this.friends.stream().noneMatch(pair -> pair.friend().equals(friend.getUUID()))) return;
        this.friends.removeIf(pair -> pair.friend().equals(friend.getUUID()));
        friend.removeFriend(this);
        friend.update();
        update();
    }

    public Optional<FriendPair> getFriendPair(UUID uuid) {
        if (deleted) return Optional.empty();
        return this.friends.stream().filter(pair -> pair.friend().equals(uuid)).findAny();
    }

    public List<UUID> getFriendRequests() {
        if (deleted) return Collections.emptyList();
        return Collections.unmodifiableList(this.friendRequests);
    }

    public List<UUID> getSentFriendRequests() {
        if (deleted) return Collections.emptyList();
        return Collections.unmodifiableList(this.sentFriendRequests);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Deletes this object instantly. No updating is required because it's done automatically.
     */
    @Override
    public void delete() {
        if (deleted) return;
        super.delete();
        this.friendRequests.forEach(uuid -> {
            Backend.getAPI().getFriendManager().getFriendHolder(uuid).ifPresent(friend -> {
                friend.sentFriendRequests.remove(this.uuid);
                friend.update();
            });
        });
        this.friendRequests.clear();
        this.sentFriendRequests.forEach(uuid -> {
            Backend.getAPI().getFriendManager().getFriendHolder(uuid).ifPresent(friend -> {
                friend.friendRequests.remove(this.uuid);
                friend.update();
            });
        });
        this.sentFriendRequests.clear();
        this.friends.forEach(pair -> {
            Backend.getAPI().getFriendManager().getFriendHolder(pair.friend()).ifPresent(friend -> {
                friend.friends.removeIf(pair1 -> pair1.friend().equals(this.uuid));
                friend.update();
            });
        });
        this.friends.clear();
    }

    @Override
    public void update() {
        Backend.getAPI().getFriendManager().updateFriendHolder(this);
    }

    @Override
    public void modify(FriendHolder friend) {
        this.deleted = friend.deleted;
        CollectionUtils.overrideCollection(this.friends, friend.friends);
        CollectionUtils.overrideCollection(this.friendRequests, friend.friendRequests);
        CollectionUtils.overrideCollection(this.sentFriendRequests, friend.sentFriendRequests);
    }
}
