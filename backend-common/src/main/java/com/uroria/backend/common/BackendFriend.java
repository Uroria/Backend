package com.uroria.backend.common;

import com.uroria.backend.common.helpers.FriendPair;
import com.uroria.backend.common.utils.ObjectUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class BackendFriend extends BackendObject<BackendFriend> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID holder;
    private final List<FriendPair> friends;
    public BackendFriend(UUID holder) {
        this.holder = holder;
        this.friends = new ArrayList<>();
    }

    public void addFriend(UUID uuid, long friendshipDate) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        for (FriendPair friendPair : this.friends) {
            if (friendPair.friend().equals(uuid)) return;
        }
        this.friends.add(new FriendPair(uuid, friendshipDate));
    }

    public void removeFriend(UUID uuid) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        this.friends.removeIf(friendPair -> friendPair.friend().equals(uuid));
    }

    public UUID getHolder() {
        return holder;
    }

    public Collection<FriendPair> getFriends() {
        return new ArrayList<>(this.friends);
    }

    @Override
    public synchronized void modify(BackendFriend friend) {
        ObjectUtils.overrideCollection(friends, friend.friends);
    }
}
