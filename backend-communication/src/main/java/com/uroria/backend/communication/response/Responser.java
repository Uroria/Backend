package com.uroria.backend.communication.response;

import com.uroria.backend.communication.request.Request;

public final class Responser<REQ extends Request, RES extends Response> {
    private final ResponsePoint point;
    private final String messageType;
    private final RequestListener<REQ, RES> listener;
    private final Class<REQ> requestClass;
    private final Class<RES> responseClass;

    Responser(ResponsePoint point, String messageType, Class<REQ> requestClass, Class<RES> responseClass, RequestListener<REQ, RES> listener) {
        this.point = point;
        this.messageType = messageType;
        this.requestClass = requestClass;
        this.responseClass = responseClass;
        this.listener = listener;
    }

    RequestListener<REQ, RES> getListener() {
        return listener;
    }

    Class<REQ> getRequestClass() {
        return requestClass;
    }

    Class<RES> getResponseClass() {
        return responseClass;
    }

    String getMessageType() {
        return messageType;
    }
}
