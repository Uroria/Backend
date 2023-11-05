package com.uroria.backend.service.modules.proxy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public final class ProxyApp {
    private final long id;
    private long lastPing;
}
