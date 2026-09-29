package com.crypto.secret.message.config;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class NatsConfig {

    @Value("${nats.url}")
    private String natsUrl;

    @Bean
    public Connection natsConnection() throws Exception {
        Options options = new Options.Builder()
                .server(natsUrl)
                .maxReconnects(-1)
                .build();

        Connection nc = Nats.connect(options);
        log.info("Connected to NATS at {}", natsUrl);
        return nc;
    }
}
