package com.zoom.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.zoom.config.ZoomApiConfig;
import com.zoom.dto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour interagir avec l'API Zoom
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ZoomApiService {

    private final WebClient webClient;
    private final ZoomApiConfig config;

    // Cache pour le token d'accès
    private String accessToken;
    private long tokenExpirationTime = 0;

    /**
     * Obtient un token d'accès OAuth pour l'API Zoom
     * Utilise le flow Server-to-Server OAuth
     */
    private String getAccessToken() {
        // Vérifie si le token est encore valide (avec marge de 5 minutes)
        if (accessToken != null && System.currentTimeMillis() < tokenExpirationTime - 300000) {
            log.debug("✓ Token existant encore valide, réutilisation");
            return accessToken;
        }

        log.info("🔐 Récupération d'un nouveau token d'accès Zoom");
        log.debug("Configuration OAuth - Auth URL: {}", config.getAuthUrl());
        log.debug("Configuration OAuth - Client ID: {}...", config.getClientId().substring(0, Math.min(10, config.getClientId().length())));
        log.debug("Configuration OAuth - Account ID: {}...", config.getAccountId().substring(0, Math.min(10, config.getAccountId().length())));

        try {
            // Encode les credentials en Base64
            String credentials = config.getClientId() + ":" + config.getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            log.debug("Credentials encodés en Base64: {}...", encodedCredentials.substring(0, Math.min(20, encodedCredentials.length())));

            // Prépare les paramètres du formulaire
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "account_credentials");
            formData.add("account_id", config.getAccountId());

            log.debug("📋 Paramètres de la requête OAuth:");
            log.debug("  URL: {}", config.getAuthUrl());
            log.debug("  Method: POST");
            log.debug("  Content-Type: {}", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            log.debug("  Authorization: Basic {}...", encodedCredentials.substring(0, Math.min(30, encodedCredentials.length())));
            log.debug("  Body parameters:");
            log.debug("    - grant_type: {}", formData.getFirst("grant_type"));
            log.debug("    - account_id: {}", formData.getFirst("account_id"));
            log.debug("  Configuration utilisée:");
            log.debug("    - Client ID: {}", config.getClientId());
            log.debug("    - Client Secret: {}...", config.getClientSecret().substring(0, Math.min(10, config.getClientSecret().length())));
            log.debug("    - Account ID: {}", config.getAccountId());

            // Effectue la requête OAuth
            log.debug("⏳ Envoi de la requête OAuth à Zoom...");

            ZoomTokenResponse response = null;
            try {
                response = webClient.post()
                        .uri(config.getAuthUrl())
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .body(BodyInserters.fromFormData(formData))
                        .retrieve()
                        .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("❌ Erreur HTTP: Status={}", clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                    .doOnNext(body -> {
                                        log.error("❌ Corps de la réponse d'erreur: {}", body);
                                        log.error("❌ Headers de la réponse: {}", clientResponse.headers().asHttpHeaders());
                                    })
                                    .flatMap(body -> clientResponse.createException());
                            }
                        )
                        .bodyToMono(ZoomTokenResponse.class)
                        .block();
            } catch (Exception e) {
                log.error("❌ Exception lors de l'appel OAuth:", e);
                log.error("❌ Type d'exception: {}", e.getClass().getName());
                log.error("❌ Message: {}", e.getMessage());
                if (e.getCause() != null) {
                    log.error("❌ Cause: {}", e.getCause().getMessage());
                }
                throw e;
            }

            if (response != null && response.getAccessToken() != null) {
                accessToken = response.getAccessToken();
                // Calcule le temps d'expiration
                tokenExpirationTime = System.currentTimeMillis() + (response.getExpiresIn() * 1000L);
                log.info("✅ Token d'accès Zoom obtenu avec succès");
                log.debug("Token: {}... (expire dans {} secondes)",
                    accessToken.substring(0, Math.min(20, accessToken.length())),
                    response.getExpiresIn());
                return accessToken;
            } else {
                log.error("❌ Réponse OAuth invalide: response={}", response);
                throw new RuntimeException("Impossible d'obtenir le token d'accès Zoom");
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'obtention du token Zoom: {}", e.getMessage());
            log.debug("Stack trace complète:", e);
            throw new RuntimeException("Erreur d'authentification Zoom: " + e.getMessage(), e);
        }
    }

    /**
     * Récupère les meetings passés de l'utilisateur pour les 5 derniers jours
     * Utilise l'API Report pour obtenir les instances/sessions réelles des meetings
     */
    public List<ZoomMeeting> getPastMeetings() {
        log.info("📅 Récupération des meetings passés des 5 derniers jours depuis Zoom");

        String token = getAccessToken();

        // Calcule les dates (5 derniers jours)
        LocalDate today = LocalDate.now();
        LocalDate fiveDaysAgo = today.minusDays(5);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String fromDate = fiveDaysAgo.format(formatter);
        String toDate = today.format(formatter);

        log.info("📆 Recherche des meetings entre {} et {}", fromDate, toDate);
        log.debug("User ID: {}", config.getUserId());

        try {
            // Utilise l'endpoint /report pour récupérer les instances réelles des meetings
            String url = config.getBaseUrl() + "/report/users/" + config.getUserId() + "/meetings" +
                    "?from=" + fromDate + "&to=" + toDate + "&page_size=300";

            log.debug("Base URL construite: {}", url);

            List<ZoomMeeting> allMeetings = new ArrayList<>();
            String nextPageToken = null;
            int pageNumber = 1;

            // Gère la pagination
            do {
                String requestUrl = url;
                if (nextPageToken != null && !nextPageToken.isEmpty()) {
                    requestUrl = url + "&next_page_token=" + nextPageToken;
                }

                log.debug("📡 [Page {}] Requête Zoom API Report: {}", pageNumber, requestUrl);
                log.debug("📡 [Page {}] Authorization: Bearer {}...", pageNumber, token.substring(0, Math.min(20, token.length())));

                ZoomMeetingResponse response = webClient.get()
                        .uri(requestUrl)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(ZoomMeetingResponse.class)
                        .block();

                log.debug("📥 [Page {}] Réponse reçue: response={}", pageNumber, response != null ? "non null" : "null");

                if (response != null) {
                    log.debug("📊 [Page {}] Détails réponse - total_records={}, page_count={}, page_size={}",
                        pageNumber, response.getTotalRecords(), response.getPageCount(), response.getPageSize());

                    if (response.getMeetings() != null) {
                        log.info("✓ [Page {}] {} sessions trouvées", pageNumber, response.getMeetings().size());

                        // Log des premiers meetings pour debug
                        response.getMeetings().stream().limit(3).forEach(m ->
                            log.debug("  - Meeting: id={}, topic='{}', start={}, duration={}min",
                                m.getId(), m.getTopic(), m.getStartTime(), m.getDuration()));

                        allMeetings.addAll(response.getMeetings());
                        nextPageToken = response.getNextPageToken();

                        log.info("📦 Total cumulé: {} sessions (nextPageToken={})",
                                allMeetings.size(), nextPageToken != null && !nextPageToken.isEmpty() ? "présent" : "absent");
                    } else {
                        log.warn("⚠️ [Page {}] Liste de meetings null dans la réponse", pageNumber);
                        nextPageToken = null;
                    }
                } else {
                    log.warn("⚠️ [Page {}] Réponse null reçue de l'API Zoom", pageNumber);
                    nextPageToken = null;
                }

                pageNumber++;

            } while (nextPageToken != null && !nextPageToken.isEmpty());

            log.info("✅ Total de {} sessions de meetings récupérées depuis Zoom", allMeetings.size());
            return allMeetings;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des meetings Zoom: {}", e.getMessage());
            log.debug("Stack trace complète:", e);
            throw new RuntimeException("Erreur lors de la récupération des meetings: " + e.getMessage(), e);
        }
    }

    /**
     * Récupère les participants d'une session spécifique
     * @param meetingUuid UUID de la session (encode automatiquement les caractères spéciaux)
     * @return Liste des participants avec leurs connexions/déconnexions
     */
    public List<ZoomParticipant> getMeetingParticipants(String meetingUuid) {
        log.info("👥 Récupération des participants pour la session UUID: {}", meetingUuid);

        String token = getAccessToken();

        try {
            // Double-encode l'UUID selon la documentation Zoom
            String encodedOnce = java.net.URLEncoder.encode(meetingUuid, "UTF-8");
            String encodedTwice = java.net.URLEncoder.encode(encodedOnce, "UTF-8");

            log.info("🔐 UUID original: {}", meetingUuid);
            log.info("🔐 UUID encodé 1x: {}", encodedOnce);
            log.info("🔐 UUID encodé 2x: {}", encodedTwice);

            List<ZoomParticipant> allParticipants = new ArrayList<>();
            String nextPageToken = null;
            int pageNumber = 1;

            // Gère la pagination
            do {
                final int currentPage = pageNumber;
                final String currentToken = nextPageToken;

                // Construit l'URL avec l'endpoint REPORT au lieu de past_meetings
                String fullUrl = config.getBaseUrl() + "/report/meetings/" + encodedTwice + "/participants?page_size=300";
                if (currentToken != null && !currentToken.isEmpty()) {
                    fullUrl += "&next_page_token=" + java.net.URLEncoder.encode(currentToken, "UTF-8");
                }

                log.info("📡 [Page {}] URL complète: {}", currentPage, fullUrl);

                // Convertit en URI pour éviter le ré-encodage par WebClient
                java.net.URI uri = java.net.URI.create(fullUrl);

                // Crée un WebClient SANS baseUrl pour cette requête spécifique
                ZoomParticipantResponse response = WebClient.create()
                        .get()
                        .uri(uri)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(ZoomParticipantResponse.class)
                        .block();

                if (response != null) {
                    log.debug("📊 [Page {}] Détails réponse - total_records={}",
                        pageNumber, response.getTotalRecords());

                    if (response.getParticipants() != null) {
                        log.info("✓ [Page {}] {} participants trouvés", pageNumber, response.getParticipants().size());
                        allParticipants.addAll(response.getParticipants());
                        nextPageToken = response.getNextPageToken();
                    } else {
                        nextPageToken = null;
                    }
                } else {
                    nextPageToken = null;
                }

                pageNumber++;

            } while (nextPageToken != null && !nextPageToken.isEmpty());

            log.info("✅ Total de {} participants récupérés", allParticipants.size());
            return allParticipants;

        } catch (WebClientResponseException e) {
            log.error("❌ Erreur HTTP {} lors de la récupération des participants", e.getStatusCode());
            log.error("❌ Message d'erreur Zoom: {}", e.getResponseBodyAsString());
            log.debug("Stack trace complète:", e);
            throw new RuntimeException("Erreur lors de la récupération des participants: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des participants: {}", e.getMessage());
            log.debug("Stack trace complète:", e);
            throw new RuntimeException("Erreur lors de la récupération des participants: " + e.getMessage(), e);
        }
    }

    /**
     * Récupère les résultats des sondages pour une session de meeting
     * @param meetingUuid UUID de la session (encodé)
     * @return Liste des réponses aux sondages
     */
    public ZoomPollResponse getPollResults(String meetingUuid) {
        log.info("📊 Récupération des résultats de sondage pour l'UUID: {}", meetingUuid);

        String token = getAccessToken();

        try {
            // Encode l'UUID une seule fois (comme pour les participants)
            String encodedOnce = java.net.URLEncoder.encode(meetingUuid, "UTF-8");

            log.info("🔐 UUID encodé 1x: {}", encodedOnce);

            // Construit l'URL
            String fullUrl = config.getBaseUrl() + "/report/meetings/" + encodedOnce + "/polls";

            log.info("📡 URL sondage: {}", fullUrl);

            // Convertit en URI pour éviter le double encodage par WebClient
            java.net.URI uri = java.net.URI.create(fullUrl);

            ZoomPollResponse response = WebClient.create()
                    .get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ZoomPollResponse.class)
                    .block();

            if (response != null && response.getParticipants() != null) {
                log.info("✅ {} réponses de sondage récupérées", response.getParticipants().size());
            } else {
                log.info("ℹ️ Aucun sondage trouvé pour cette session");
            }

            return response;

        } catch (WebClientResponseException e) {
            log.error("❌ Erreur HTTP {} lors de la récupération des sondages", e.getStatusCode());
            log.error("❌ Message d'erreur Zoom: {}", e.getResponseBodyAsString());

            // Si 404, c'est qu'il n'y a pas de sondage
            if (e.getStatusCode().value() == 404) {
                log.info("ℹ️ Aucun sondage disponible pour cette session");
                return null;
            }

            throw new RuntimeException("Erreur lors de la récupération des sondages: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des sondages: {}", e.getMessage());
            log.debug("Stack trace complète:", e);
            throw new RuntimeException("Erreur lors de la récupération des sondages: " + e.getMessage(), e);
        }
    }
}

