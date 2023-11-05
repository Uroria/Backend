package com.uroria.backend.impl;

import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.WrapperManager;
import org.slf4j.Logger;

public abstract class StatedManager<T extends Wrapper> extends WrapperManager<T> {
    protected final BackendWrapperImpl wrapper;

    public StatedManager(Logger logger, BackendWrapperImpl wrapper, String requestPointTopic, String broadcastPointTopic, String topic) {
        super(logger, wrapper.getCommunicator(), requestPointTopic, broadcastPointTopic, topic);
        this.wrapper = wrapper;
    }

    public StatedManager(Logger logger, BackendWrapperImpl wrapper, String topic) {
        this(logger, wrapper, topic, topic, topic);
    }
}
