package com.uroria.backend.server;

import com.uroria.backend.user.User;

import java.util.Collection;

public interface ServerGroupTarget {

    String getType();

    Collection<User> getOnlineUsers();

    int getOnlineUserCount();

    int getMaxUserCount();
}
