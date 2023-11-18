package com.uroria.backend.impl.server.group;

import com.uroria.backend.Backend;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.impl.server.ServerManager;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ServerGroupWrapper extends Wrapper implements ServerGroup {
    private final ServerManager serverManager;
    private final String name;

    private boolean deleted;

    public ServerGroupWrapper(WrapperManager<? extends Wrapper> wrapperManager, ServerManager serverManager, String name) {
        super(wrapperManager);
        this.serverManager = serverManager;
        this.name = name;
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        getServers().forEach(server -> {
            server.setStatus(ApplicationStatus.STOPPED);
            server.delete();
        });
        object.set("deleted", true);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        boolean deleted = this.object.getBooleanOrElse("deleted", false);
        this.deleted = deleted;
        return deleted;
    }

    @Override
    public Optional<Server> getServerByIdentifier(long identifier) {
        return getRawServers().stream()
                .filter(id -> id == identifier)
                .findAny()
                .map(id -> Backend.server(identifier).get());
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public Collection<Server> getServersWithTemplateId(int templateId) {
        return getRawServers().stream()
                .map(identifier -> Backend.server(identifier).get())
                .filter(this::nullCheck)
                .filter(server -> server.getTemplateId() == templateId)
                .toList();
    }

    @Override
    public Collection<Server> getServers() {
        return getRawServers().stream()
                .map(identifier -> Backend.server(identifier).get())
                .filter(this::nullCheck)
                .toList();
    }

    public void addServer(long id) {
        Set<Long> servers = getRawServers();
        servers.add(id);
        this.object.set("servers", servers);
    }

    public void removeServer(long id) {
        Set<Long> servers = getRawServers();
        servers.remove(id);
        this.object.set("servers", servers);
    }

    private Set<Long> getRawServers() {
        return this.object.getSet("servers", Long.class);
    }

    @Override
    public Result<Server> createServer(int templateId) {
        try {
            return Result.of(this.serverManager.createServerWrapper(this, templateId));
        } catch (Exception exception) {
            getLogger().error("Cannot create server by group " + getName() + " with templateId " + templateId, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Collection<User> getOnlineUsers() {
        return this.object.getSet("onlineUsers", String.class).stream()
                .map(uuidString -> {
                    try {
                        return Backend.user(UUID.fromString(uuidString)).get();
                    } catch (Exception exception) {
                        return null;
                    }
                })
                .filter(this::nullCheck)
                .toList();
    }

    @Override
    public int getOnlineUserCount() {
        return this.object.getIntOrElse("playerCount", 0);
    }

    @Override
    public int getMaxUserCount() {
        return this.object.getIntOrElse("maxPlayerCount", 0);
    }

    @Override
    public String getIdentifier() {
        return this.name;
    }
}
