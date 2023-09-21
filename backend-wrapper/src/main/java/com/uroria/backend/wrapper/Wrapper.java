package com.uroria.backend.wrapper;

import com.uroria.backend.BackendWrapper;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.AbstractBackendWrapper;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;
import com.uroria.backend.wrapper.user.UserManager;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.Optional;
import java.util.UUID;

public final class Wrapper extends AbstractBackendWrapper implements BackendWrapper {
    private final UserManager userManager;

    public Wrapper() {
        this.userManager = new UserManager(getPulsarClient(), getCryptoKeyReader());
    }

    @Override
    public void start() throws PulsarClientException {
        this.userManager.start();
    }

    @Override
    public void shutdown() throws PulsarClientException {
        this.userManager.shutdown();
        super.shutdown();
    }

    @Override
    public Optional<User> getUser(UUID uuid) {
        return Optional.ofNullable(this.userManager.getUser(uuid));
    }

    @Override
    public Optional<User> getUser(String username) {
        return Optional.ofNullable(this.userManager.getUser(username));
    }

    @Override
    public Optional<Clan> getClan(String tag) {
        return Optional.empty();
    }

    @Override
    public Optional<Server> getServer(long identifier) {
        return Optional.empty();
    }

    @Override
    public Optional<ServerGroup> getServerGroup(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<PermGroup> getPermissionGroup(String name) {
        return Optional.empty();
    }
}
