package com.uroria.backend.impl.communication.response;

import com.uroria.backend.impl.communication.TopicHolder;
import com.uroria.problemo.result.Result;

import java.io.Closeable;

public interface ResponseChannel extends TopicHolder, Closeable {

    Result<Request> awaitRequest();

    Result<Request> awaitRequest(int timeoutMs);
}
