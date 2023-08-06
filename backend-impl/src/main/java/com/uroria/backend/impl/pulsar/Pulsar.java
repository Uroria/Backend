package com.uroria.backend.impl.pulsar;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UtilityClass
public class Pulsar {
    private @Getter final Logger logger = LoggerFactory.getLogger("Pulsar");
}
