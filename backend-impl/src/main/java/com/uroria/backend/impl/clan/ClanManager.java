package com.uroria.backend.impl.clan;

import com.rabbitmq.client.Connection;
import com.uroria.backend.clan.events.ClanDeletedEvent;
import com.uroria.backend.clan.events.ClanUpdatedEvent;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.wrapper.WrapperManager;
import org.slf4j.LoggerFactory;

public final class ClanManager extends WrapperManager<ClanWrapper> {
    public ClanManager(Connection rabbit) {
        super(rabbit, LoggerFactory.getLogger("Clans"), "clan", "name");
    }

    @Override
    protected void onUpdate(ClanWrapper wrapper) {
        if (wrapper.isDeleted()) {
            eventManager.callAndForget(new ClanDeletedEvent(wrapper));
        }
        eventManager.callAndForget(new ClanUpdatedEvent(wrapper));
    }

    public ClanWrapper createClanWrapper(String name, String tag, long foundingDate) {
        ClanWrapper wrapper = new ClanWrapper(this.client, name);
        CommunicationWrapper object = wrapper.getObjectWrapper();
        wrappers.add(wrapper);
        object.set("foundingDate", foundingDate);
        object.set("tag", tag);
        return getWrapper(tag, true);
    }

    public ClanWrapper getClanWrapper(String tag) {
        return getWrapper(tag, false);
    }


    @Override
    protected ClanWrapper createWrapper(String identifier) {
        return new ClanWrapper(this.client, identifier);
    }
}
