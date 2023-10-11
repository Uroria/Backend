package com.uroria.backend.impl.configurations;

import com.uroria.base.configs.InternalConfigurations;
import com.uroria.fastconfig.Json;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RabbitConfiguration {
    private final Json config = InternalConfigurations.create("rabbit", RabbitConfiguration.class.getResourceAsStream("rabbit.json"));

    private @Getter String username;
    private @Getter String password;
    private @Getter String virtualHost;
    private @Getter String hostname;
    private @Getter String sslCertPath, sslKeyStorePath, sslKeyStorePassword, sslCertPassword;
    private @Getter boolean sslEnabled;
    private @Getter int port;

    static {
        reload();
    }

    public void reload() {
        config.reload();

        username = config.getOrSetDefault("rabbit.credentials.username", "guest");
        password = config.getOrSetDefault("rabbit.credentials.password", "guest");
        virtualHost = config.getOrSetDefault("rabbit.connection.virtualhost", "/");
        hostname = config.getOrSetDefault("rabbit.connection.hostname", "localhost");
        sslEnabled = config.getOrSetDefault("rabbit.connection.sslEnabled", true);
        port = config.getOrSetDefault("rabbit.connection.port", 5671);

        sslCertPath = config.getOrSetDefault("rabbit.ssl.certPath", "/path/to/key.pks");
        sslCertPassword = config.getOrSetDefault("rabbit.ssl.certPassword", "12345678");
        sslKeyStorePath = config.getOrSetDefault("rabbit.ssl.keyStorePath", "/path/to/keystore");
        sslKeyStorePassword = config.getOrSetDefault("rabbit.ssl.keyStorePassword", "123456");
    }
}
