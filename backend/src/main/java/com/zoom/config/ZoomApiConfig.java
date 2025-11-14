package com.zoom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration pour l'API Zoom
 */
@Configuration
@ConfigurationProperties(prefix = "zoom.api")
@Data
public class ZoomApiConfig {

    private String baseUrl;
    private String authUrl;
    private String clientId;
    private String clientSecret;
    private String accountId;
    private String userId;
}
