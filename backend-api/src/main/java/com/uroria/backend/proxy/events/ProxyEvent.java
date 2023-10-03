package com.uroria.backend.proxy.events;

import com.uroria.backend.proxy.Proxy;
import lombok.NonNull;

public abstract class ProxyEvent {
    private final Proxy proxy;

    public ProxyEvent(@NonNull Proxy proxy) {
        this.proxy = proxy;
    }

    public Proxy getProxy() {
        return proxy;
    }
}
