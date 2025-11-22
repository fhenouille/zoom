package com.zoom.security;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Intercepteur pour le rate limiting sur les endpoints sensibles
 * Protège contre les brute force attacks
 */
@Component
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Configuration des limites par endpoint
     */
    private static final int LOGIN_RATE_LIMIT = 5; // 5 tentatives par minute

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String requestUri = request.getRequestURI();

        // Appliquer rate limiting sur /api/auth/login
        if (requestUri.equals("/api/auth/login")) {
            String clientId = getClientIdentifier(request);
            Bucket bucket = resolveBucket(clientId);

            if (bucket.tryConsume(1)) {
                // Requête autorisée
                log.debug("✓ Rate limit OK pour {}", clientId);
                return true;
            } else {
                // Trop de requêtes
                log.warn("⚠️ Rate limit dépassé pour {}", clientId);
                response.setStatus(429); // 429 Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Trop de tentatives. Réessayez après quelques minutes.\"}");
                return false;
            }
        }

        return true;
    }

    /**
     * Obtient ou crée un bucket pour le client
     */
    private Bucket resolveBucket(String clientId) {
        return cache.computeIfAbsent(clientId, key -> createNewBucket());
    }

    /**
     * Crée un nouveau bucket avec les limites de rate limiting
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(LOGIN_RATE_LIMIT,
            Refill.intervally(LOGIN_RATE_LIMIT, Duration.ofMinutes(1)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Identifie le client (IP ou utilisateur)
     */
    private String getClientIdentifier(HttpServletRequest request) {
        // Essayer de récupérer l'IP réelle (derrière un proxy)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return "login:" + ip;
    }
}
