package com.uroria.backend.impl.clan;

import com.google.gson.JsonElement;
import com.rabbitmq.client.Connection;
import com.uroria.backend.Backend;
import com.uroria.backend.clan.events.ClanDeletedEvent;
import com.uroria.backend.clan.events.ClanUpdatedEvent;
import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.request.RabbitRequestChannel;
import com.uroria.backend.impl.communication.request.RequestChannel;
import com.uroria.base.event.EventManager;
import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClanManager extends AbstractManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Clans");

    private final CommunicationClient client;
    private final RequestChannel request;
    private final ObjectSet<ClanWrapper> clans;

    public ClanManager(Connection rabbit, Logger logger) {
        super(rabbit, logger);
        this.clans = new ObjectArraySet<>();
        EventManager eventManager = Backend.getEventManager();
        this.client = new CommunicationClient(rabbit, "clan-request", "clan-update", element -> {
            JsonElement nameElement = element.get("name");
            if (nameElement == null) return;
            String name = nameElement.getAsString();
            boolean delete = false;
            for (ClanWrapper wrapper : this.clans) {
                if (!wrapper.getName().equals(name)) continue;
                if (wrapper.isDeleted()) {
                    delete = true;
                    eventManager.callAndForget(new ClanDeletedEvent(wrapper));
                }
                eventManager.callAndForget(new ClanUpdatedEvent(wrapper));
                break;
            }
            if (delete) delete(name);
        });
        this.request = new RabbitRequestChannel(rabbit, "clan-requests");
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        this.request.close();
        this.client.close();
    }

    public void delete(String name) {
        this.clans.removeIf(clan -> clan.getName().equals(name));
        this.client.delete(name);
    }

    public ClanWrapper getWrapper(String tag) {
        for (ClanWrapper wrapper : this.clans) {
            if (!wrapper.getTag().equals(tag)) continue;
            return wrapper;
        }

        byte[] data;
        try {
            InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream();
            output.writeUTF(tag);
            output.close();
            data = output.toByteArray();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        Result<byte[]> result = this.request.requestSync(data, 4000);

        if (result instanceof Result.Problematic<byte[]> problem) {
            LOGGER.error("Cannot request clan " + tag, problem.getProblem().getError().orElse(new RuntimeException("Unknown error")));
            return null;
        }

        byte[] bytes = result.get();
        if (bytes == null) {
            return null;
        }

        try {
            InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(bytes);
            String name = input.readUTF();
            input.close();
            ClanWrapper wrapper = new ClanWrapper(this.client, name);
            wrapper.getObject().addProperty("name", name);
            this.clans.add(wrapper);
            return wrapper;
        } catch (Exception exception) {
            LOGGER.error("Cannot read clan " + tag, exception);
            return null;
        }
    }
}
