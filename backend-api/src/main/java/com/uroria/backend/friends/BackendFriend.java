package com.uroria.backend.friends;

import com.uroria.backend.BackendObject;
import com.uroria.backend.helpers.FriendPair;
import com.uroria.backend.utils.ObjectUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class BackendFriend extends BackendObject<BackendFriend> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID holder;
    private final Set<FriendPair> friends;
    public BackendFriend(@NonNull UUID holder) {
        this.holder = holder;
        this.friends = new ObjectArraySet<>();
    }

    public void addFriend(@NonNull UUID uuid, long friendshipDate) {
        for (FriendPair friendPair : this.friends) {
            if (friendPair.friend().equals(uuid)) return;
        }
        this.friends.add(new FriendPair(uuid, friendshipDate));
    }

    public void removeFriend(@NonNull UUID uuid) {
        this.friends.removeIf(friendPair -> friendPair.friend().equals(uuid));
    }

    public UUID getHolder() {
        return holder;
    }

    public Set<FriendPair> getFriends() {
        return Collections.unmodifiableSet(this.friends);
    }

    @Override
    public synchronized void modify(BackendFriend friend) {
        ObjectUtils.overrideCollection(friends, friend.friends);
    }
}
