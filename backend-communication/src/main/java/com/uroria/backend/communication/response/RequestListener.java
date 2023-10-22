package com.uroria.backend.communication.response;

import com.uroria.backend.communication.request.Request;

import java.util.Optional;

public abstract class RequestListener<REQ extends Request, RES extends Response> {

    protected abstract Optional<RES> onRequest(REQ request);

}
