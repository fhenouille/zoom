package com.zoom.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests du Rate Limiting Interceptor
 */
@ExtendWith(MockitoExtension.class)
class RateLimitingInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private RateLimitingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
    }

    @Test
    void testLoginEndpointAllowsFirstAttempts() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // Les 5 premières tentatives doivent être acceptées
        for (int i = 0; i < 5; i++) {
            assertTrue(interceptor.preHandle(request, response, null),
                    "Tentative " + (i + 1) + " devrait être acceptée");
        }
    }

    @Test
    void testLoginEndpointBlocksAfterRateLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // Épuiser le quota
        for (int i = 0; i < 5; i++) {
            interceptor.preHandle(request, response, null);
        }

        // La 6ème tentative doit être bloquée
        assertFalse(interceptor.preHandle(request, response, null),
                "La 6ème tentative devrait être bloquée");
        verify(response).setStatus(429);
    }

    @Test
    void testOtherEndpointsNotRateLimited() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/meetings");

        // Les autres endpoints ne doivent pas être affectés
        for (int i = 0; i < 100; i++) {
            assertTrue(interceptor.preHandle(request, response, null),
                    "L'endpoint /api/meetings ne doit pas être rate limité");
        }
    }
}
