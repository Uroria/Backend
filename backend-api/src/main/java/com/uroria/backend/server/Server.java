package com.uroria.backend.server;

import com.uroria.problemo.result.Result;

import java.net.InetSocketAddress;

public interface Server extends ServerGroupTarget {

    long getIdentifier();

    ServerGroup getGroup();

    ServerStatus getStatus();

    Result<InetSocketAddress> getAddress();

    int getTemplateId();
}
