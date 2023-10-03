package com.uroria.backend.impl.configurations;

import com.uroria.base.configs.InternalConfigurations;
import com.uroria.fastconfig.Json;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RabbitConfiguration {
    private final Json config = InternalConfigurations.create("rabbit", RabbitConfiguration.class.getResourceAsStream("rabbit.json"));

    private @Getter String rabbitUsername;
    private @Getter String rabbitPassword;
    private @Getter String rabbitVirtualhost;
    private @Getter String rabbitHostname;
    private @Getter boolean rabbitSslEnabled;
    private @Getter int rabbitPort;

    static {
        reload();
    }

    public void reload() {
        config.reload();

        rabbitUsername = config.getOrSetDefault("rabbit.credentials.username", "guest");
        rabbitPassword = config.getOrSetDefault("rabbit.credentials.password", "guest");
        rabbitVirtualhost = config.getOrSetDefault("rabbit.connection.virtualhost", "/");
        rabbitHostname = config.getOrSetDefault("rabbit.connection.hostname", "localhost");
        rabbitSslEnabled = config.getOrSetDefault("rabbit.connection.sslEnabled", true);
        rabbitPort = config.getOrSetDefault("rabbit.connection.port", 5671);
    }
}
