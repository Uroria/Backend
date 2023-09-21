package com.uroria.backend.impl.configurations;

import com.uroria.base.configs.InternalConfigurations;
import com.uroria.fastconfig.Json;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;

@UtilityClass
public class PulsarConfiguration {
    private final Json config = InternalConfigurations.create("pulsar", PulsarConfiguration.class.getResourceAsStream("pulsar.json"));

    private @Getter boolean pulsarEncryptionEnabled;
    private @Getter String pulsarEncryptionPublicKey;
    private @Getter String pulsarEncryptionPrivateKey;

    private @Getter boolean pulsarJWTAuthEnabled;
    private @Getter String pulsarJWTAuthToken;

    private @Getter String pulsarPrimaryUrl;
    private @Getter String[] pulsarBackupUrls;

    static {
        reload();
    }

    public void reload() {
        config.reload();

        pulsarEncryptionEnabled = config.getOrSetDefault("pulsar.encryption.enable", false);
        pulsarEncryptionPublicKey = config.getOrSetDefault("pulsar.encryption.publicKey", "");
        pulsarEncryptionPrivateKey = config.getOrSetDefault("pulsar.encryption.privateKey", "");

        pulsarJWTAuthEnabled = config.getOrSetDefault("pulsar.jwtAuth.enabled", false);
        pulsarJWTAuthToken = config.getOrSetDefault("pulsar.jwtAuth.token", "");

        pulsarPrimaryUrl = config.getOrSetDefault("pulsar.primaryUrl", "");
        pulsarBackupUrls = config.getOrSetDefault("pulsar.urls", new ArrayList<String>()).toArray(String[]::new);
    }
}
