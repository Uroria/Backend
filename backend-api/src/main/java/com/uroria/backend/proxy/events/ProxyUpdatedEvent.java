package com.uroria.backend.proxy.events;

import com.uroria.backend.proxy.Proxy;
import lombok.NonNull;

public final class ProxyUpdatedEvent extends ProxyEvent {
    public ProxyUpdatedEvent(@NonNull Proxy proxy) {
        super(proxy);
    }
}
