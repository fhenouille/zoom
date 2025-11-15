package com.zoom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration du WebClient pour les appels HTTP
 */
@Configuration
public class WebClientConfig {

    @Value("${zoom.api.base-url}")
    private String zoomBaseUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(zoomBaseUrl)
                .build();
    }
}
