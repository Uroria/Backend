package com.uroria.backend.server;

import com.uroria.annotations.safety.TimeConsuming;
import com.uroria.backend.Deletable;
import com.uroria.backend.user.User;

import java.util.Collection;

public interface ServerGroupTarget extends Deletable {

    String getType();

    @TimeConsuming
    Collection<User> getOnlineUsers();

    int getOnlineUserCount();

    int getMaxUserCount();
}
