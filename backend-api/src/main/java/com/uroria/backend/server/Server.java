package com.uroria.backend.server;

import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public interface Server extends ServerGroupTarget {

    long getIdentifier();

    ServerGroup getGroup();

    ServerStatus getStatus();

    @Nullable InetSocketAddress getAddress();

    int getTemplateId();
}
