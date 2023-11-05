package com.uroria.backend.impl.user;

import com.uroria.backend.cache.communication.controls.UnavailableException;
import com.uroria.backend.cache.communication.user.GetUserRequest;
import com.uroria.backend.cache.communication.user.GetUserResponse;
import com.uroria.backend.communication.request.Requester;
import com.uroria.backend.impl.BackendWrapperImpl;
import com.uroria.backend.impl.StatedManager;
import com.uroria.backend.impl.stats.StatsManager;
import com.uroria.backend.user.events.UserDeletedEvent;
import com.uroria.backend.user.events.UserUpdatedEvent;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class UserManager extends StatedManager<UserWrapper> {
    private static final Logger logger = LoggerFactory.getLogger("Users");

    private final StatsManager statsManager;
    private final Requester<GetUserRequest, GetUserResponse> check;

    public UserManager(BackendWrapperImpl wrapper) {
        super(logger, wrapper, "user");
        this.statsManager = wrapper.getStatsManager();
        this.check = requestPoint.registerRequester(GetUserRequest.class, GetUserResponse.class, "Check");
    }

    public UserWrapper getUserWrapper(UUID uuid) {
        for (UserWrapper wrapper : this.wrappers) {
            if (wrapper.getUniqueId().equals(uuid)) return wrapper;
        }

        if (!wrapper.isAvailable()) throw new UnavailableException();

        Result<GetUserResponse> result = this.check.request(new GetUserRequest(uuid, null, true), 5000);
        GetUserResponse response = result.get();
        if (response == null || !response.isExistent()) return null;
        UserWrapper wrapper = new UserWrapper(this, uuid, this.statsManager);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    public UserWrapper getUserWrapper(String username) {
        for (UserWrapper wrapper : this.wrappers) {
            if (wrapper.getUsername().equals(username)) return wrapper;
        }

        if (!wrapper.isAvailable()) throw new UnavailableException();

        Result<GetUserResponse> result = this.check.request(new GetUserRequest(null, username, false), 5000);
        GetUserResponse response = result.get();
        if (response == null || !response.isExistent()) return null;
        UserWrapper wrapper = new UserWrapper(this, response.getUuid(), this.statsManager);
        wrapper.setUsername(username);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    @Override
    protected void onUpdate(UserWrapper wrapper) {
        if (wrapper.isDeleted()) this.eventManager.callAndForget(new UserDeletedEvent(wrapper));
        else this.eventManager.callAndForget(new UserUpdatedEvent(wrapper));
    }
}
