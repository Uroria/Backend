package com.uroria.backend.impl;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.uroria.backend.impl.configurations.RabbitConfiguration;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

@UtilityClass
public class RabbitUtils {

    public Connection buildConnection(Logger logger) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(RabbitConfiguration.getUsername());
            factory.setPassword(RabbitConfiguration.getPassword());
            factory.setVirtualHost(RabbitConfiguration.getVirtualHost());
            factory.setHost(RabbitConfiguration.getHostname());
            factory.setPort(RabbitConfiguration.getPort());
            if (RabbitConfiguration.isSslEnabled()) {
                KeyStore ks = KeyStore.getInstance("PKCS12");
                ks.load(new FileInputStream(RabbitConfiguration.getSslCertPath()), RabbitConfiguration.getSslCertPassword().toCharArray());

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, RabbitConfiguration.getSslCertPassword().toCharArray());

                KeyStore tks = KeyStore.getInstance("JKS");
                tks.load(new FileInputStream(RabbitConfiguration.getSslKeyStorePath()), RabbitConfiguration.getSslKeyStorePassword().toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(tks);

                SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

                factory.useSslProtocol(sslContext);
                factory.enableHostnameVerification();
            }
            return factory.newConnection();
        } catch (Exception exception) {
            logger.error("Cannot connect to RabbitMQ", exception);
            System.exit(1);
            return null;
        }
    }
}

