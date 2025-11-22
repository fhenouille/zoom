package com.zoom.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * Configuration du WebClient pour les appels HTTP avec timeouts
 */
@Configuration
public class WebClientConfig {

    @Value("${zoom.api.base-url}")
    private String zoomBaseUrl;

    @Bean
    public WebClient webClient() {
        // Configuration du connection pool
        ConnectionProvider connectionProvider = ConnectionProvider.builder("zoom-api")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(30))
                .maxLifeTime(Duration.ofMinutes(5))
                .build();

        // Configuration du HttpClient avec timeouts
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofSeconds(10))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .doOnConnected(connection ->
                    connection.addHandlerLast(new ReadTimeoutHandler(10))
                              .addHandlerLast(new WriteTimeoutHandler(10))
                );

        return WebClient.builder()
                .baseUrl(zoomBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
