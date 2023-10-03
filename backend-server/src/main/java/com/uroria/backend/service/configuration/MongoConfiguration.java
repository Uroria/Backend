package com.uroria.backend.service.configuration;

import com.uroria.base.configs.InternalConfigurations;
import com.uroria.fastconfig.Json;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MongoConfiguration {
    private final Json config = InternalConfigurations.create("mongo", MongoConfiguration.class.getResourceAsStream("mongo.json"));

    @Getter private String mongoUrl;
    @Getter private String mongoDatabase;

    static {
        reload();
    }

    public void reload() {
        mongoUrl = config.getOrSetDefault("mongo.url", "localhost");
        mongoDatabase = config.getOrSetDefault("mongo.database", "backend");
    }
}
