package com.uroria.backend.proxy.events;

import com.uroria.backend.proxy.Proxy;
import lombok.NonNull;

public final class ProxyDeletedEvent extends ProxyEvent {
    public ProxyDeletedEvent(@NonNull Proxy proxy) {
        super(proxy);
    }
}
