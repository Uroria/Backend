package com.uroria.backend.service.modules.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public final class ServerApp {
    private final long id;
    private long lastPing;
}
