package com.uroria.backend.impl.clan;

import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.cache.communication.clan.CheckClanRequest;
import com.uroria.backend.cache.communication.clan.CheckClanResponse;
import com.uroria.backend.cache.communication.clan.GetClanRequest;
import com.uroria.backend.cache.communication.clan.GetClanResponse;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.broadcast.BroadcastPoint;
import com.uroria.backend.communication.request.RequestPoint;
import com.uroria.backend.communication.request.Requester;
import com.uroria.problemo.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClanManager extends WrapperManager<ClanWrapper> {
    private static final Logger logger = LoggerFactory.getLogger("Clans");

    private final Requester<CheckClanRequest, CheckClanResponse> tagCheck;
    private final Requester<GetClanRequest, GetClanResponse> nameCheck;

    public ClanManager(Communicator communicator) {
        super(logger, new RequestPoint(communicator, "clans"), new BroadcastPoint(communicator, "clans"), "clans");
        this.tagCheck = requestPoint.registerRequester(CheckClanRequest.class, CheckClanResponse.class, "CheckTag");
        this.nameCheck = requestPoint.registerRequester(GetClanRequest.class, GetClanResponse.class, "CheckName");
    }

    public ClanWrapper getClanWrapper(String tag) {
        for (ClanWrapper wrapper : this.wrappers) {
            if (wrapper.getTag().equals(tag)) return wrapper;
        }

        Result<CheckClanResponse> result = this.tagCheck.request(new CheckClanRequest(tag), 2000);
        CheckClanResponse response = result.get();
        if (response == null) return null;
        if (!response.isExistent()) return null;
        return getClanWrapperByName(response.getName());
    }

    public ClanWrapper getClanWrapperByName(String name) {
        for (ClanWrapper wrapper : this.wrappers) {
            if (wrapper.getName().equals(name)) return wrapper;
        }

        Result<GetClanResponse> request = this.nameCheck.request(new GetClanRequest(name), 2000);
        GetClanResponse response = request.get();
        if (response == null) return null;
        if (!response.isExistent()) return null;
        ClanWrapper wrapper = new ClanWrapper(this, name);
        this.wrappers.add(wrapper);
        return wrapper;
    }
}
