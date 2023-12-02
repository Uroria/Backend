package com.uroria.backend.impl.proxy;

import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.cache.BackendObject;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.communication.controls.UnavailableException;
import com.uroria.backend.cache.communication.proxy.GetAllProxiesRequest;
import com.uroria.backend.cache.communication.proxy.GetAllProxiesResponse;
import com.uroria.backend.cache.communication.proxy.GetProxyRequest;
import com.uroria.backend.cache.communication.proxy.GetProxyResponse;
import com.uroria.backend.communication.request.Requester;
import com.uroria.backend.impl.BackendWrapperImpl;
import com.uroria.backend.impl.StatedManager;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.proxy.events.ProxyDeletedEvent;
import com.uroria.backend.proxy.events.ProxyUpdatedEvent;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Random;

public final class ProxyManager extends StatedManager<ProxyWrapper> {
    private static final Logger logger = LoggerFactory.getLogger("Proxies");

    private final Requester<GetProxyRequest, GetProxyResponse> idCheck;
    private final Requester<GetAllProxiesRequest, GetAllProxiesResponse> allGet;

    public ProxyManager(BackendWrapperImpl wrapper) {
        super(logger, wrapper, "proxy");
        this.idCheck = requestPoint.registerRequester(GetProxyRequest.class, GetProxyResponse.class, "CheckId");
        this.allGet = requestPoint.registerRequester(GetAllProxiesRequest.class, GetAllProxiesResponse.class, "GetAll");
    }

    public Collection<Proxy> getAllWithName(String name) {
        if (!wrapper.isAvailable()) throw new UnavailableException();
        Result<GetAllProxiesResponse> result = allGet.request(new GetAllProxiesRequest(name), 5000);
        GetAllProxiesResponse response = result.get();
        if (response == null) return ObjectSets.emptySet();
        ObjectSet<Proxy> wrappers = new ObjectArraySet<>();
        response.getProxies().forEach(id -> {
            ProxyWrapper wrapper = getProxyWrapper(id);
            if (wrapper == null) {
                logger.error("Cannot get proxy with id " + id + " while getting all with name " + name);
                return;
            }
            String wrapperName = wrapper.getName();
            if (!wrapperName.equals(name)) {
                logger.warn("Proxy " + id + " name " + wrapperName + " isn't equal to " + name + " while getting all");
                return;
            }
            wrappers.add(wrapper);
        });
        return wrappers;
    }

    public Collection<Proxy> getAll() {
        if (!wrapper.isAvailable()) throw new UnavailableException();
        Result<GetAllProxiesResponse> result = allGet.request(new GetAllProxiesRequest(null), 5000);
        GetAllProxiesResponse response = result.get();
        if (response == null) return ObjectSets.emptySet();
        ObjectSet<Proxy> wrappers = new ObjectArraySet<>();
        response.getProxies().forEach(id -> {
            ProxyWrapper wrapper = getProxyWrapper(id);
            if (wrapper == null) {
                logger.error("Cannot get proxy with id " + id + " while getting all");
                return;
            }
            wrappers.add(wrapper);
        });
        return wrappers;
    }

    public ProxyWrapper getProxyWrapper(long id) {
        for (ProxyWrapper wrapper : this.wrappers) {
            if (wrapper.getId() == id) return wrapper;
        }

        if (!wrapper.isAvailable()) throw new UnavailableException();

        Result<GetProxyResponse> result = this.idCheck.request(new GetProxyRequest(id, false), 2000);
        GetProxyResponse response = result.get();
        if (response == null) return null;
        if (!response.isExistent()) return null;
        ProxyWrapper wrapper = new ProxyWrapper(this, id);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    public ProxyWrapper createProxyWrapper(String name, int templateId, int maxPlayers) {
        if (!wrapper.isAvailable()) throw new UnavailableException();
        long id = System.currentTimeMillis() + new Random().nextLong(10000) - maxPlayers - templateId;
        Result<GetProxyResponse> result = this.idCheck.request(new GetProxyRequest(id, true), 5000);
        if (result instanceof Result.Problematic<GetProxyResponse> problematic) {
            logger.error("Cannot create new proxy wrapper", problematic.getAsProblematic().getProblem().getError().orElse(null));
            return null;
        }
        GetProxyResponse response = result.get();
        if (response == null) return null;
        if (!response.isExistent()) return null;
        ProxyWrapper wrapper = new ProxyWrapper(this, id);
        BackendObject<? extends Wrapper> object = wrapper.getBackendObject();
        object.set("id", id);
        object.set("maxPlayers", maxPlayers);
        object.set("templateId", templateId);
        object.set("name", name);
        wrapper.setStatus(ApplicationStatus.EMPTY);
        this.wrappers.add(wrapper);
        return wrapper;
    }

    @Override
    protected void onUpdate(ProxyWrapper wrapper) {
        if (wrapper.isDeleted()) {
            this.eventManager.callAndForget(new ProxyDeletedEvent(wrapper));
            this.wrappers.remove(wrapper);
        }
        else this.eventManager.callAndForget(new ProxyUpdatedEvent(wrapper));
    }
}
