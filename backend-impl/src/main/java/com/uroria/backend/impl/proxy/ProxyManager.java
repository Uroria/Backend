package com.uroria.backend.impl.proxy;

import com.rabbitmq.client.Connection;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.wrapper.WrapperManager;
import com.uroria.backend.proxy.events.ProxyDeletedEvent;
import com.uroria.backend.proxy.events.ProxyUpdatedEvent;
import org.slf4j.LoggerFactory;

import java.util.Random;

public final class ProxyManager extends WrapperManager<ProxyWrapper> {
    public ProxyManager(Connection rabbit) {
        super(rabbit, LoggerFactory.getLogger("Proxies"), "proxy", "identifier");
    }

    @Override
    protected void onUpdate(ProxyWrapper wrapper) {
        if (wrapper.isDeleted()) {
            eventManager.callAndForget(new ProxyDeletedEvent(wrapper));
        }
        eventManager.callAndForget(new ProxyUpdatedEvent(wrapper));
    }

    public ProxyWrapper getProxyWrapper(long identifier) {
        return getWrapper(String.valueOf(identifier), false);
    }

    public ProxyWrapper createProxyWrapper(String name, int maxPlayers) {
        long identifier = new Random().nextLong() + System.currentTimeMillis();
        ProxyWrapper wrapper = new ProxyWrapper(this.client, identifier);
        this.wrappers.add(wrapper);
        CommunicationWrapper object = wrapper.getObjectWrapper();
        object.set("identifier", identifier);
        object.set("name", name);
        object.set("maxPlayers", maxPlayers);
        object.set("status", ApplicationStatus.EMPTY.getID());
        return getWrapper(String.valueOf(identifier), true);
    }

    @Override
    protected ProxyWrapper createWrapper(String identifier) {
        return new ProxyWrapper(this.client, Long.parseLong(identifier));
    }
}
