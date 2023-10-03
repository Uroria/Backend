package com.uroria.backend.service.configuration;

import com.uroria.base.configs.InternalConfigurations;
import com.uroria.fastconfig.Json;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RedisConfiguration {
    private final Json config = InternalConfigurations.create("redis", RedisConfiguration.class.getResourceAsStream("redis.json"));

    @Getter private String redisUrl;

    static {
        reload();
    }

    public void reload() {
        redisUrl = config.getOrSetDefault("redis.url", "localhost");
    }
}
