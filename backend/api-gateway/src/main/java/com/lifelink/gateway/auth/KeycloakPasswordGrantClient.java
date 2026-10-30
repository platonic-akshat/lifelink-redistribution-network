package com.lifelink.gateway.auth;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class KeycloakPasswordGrantClient {

    private final WebClient webClient;
    private final String tokenUrl;
    private final String clientId;

    public KeycloakPasswordGrantClient(
            WebClient.Builder webClientBuilder,
            @Value("${lifelink.auth.keycloak-token-url:http://localhost:8180/realms/lifelink/protocol/openid-connect/token}") String tokenUrl,
            @Value("${lifelink.auth.keycloak-client-id:lifelink-web}") String clientId) {
        this.webClient = webClientBuilder.build();
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
    }

    public Mono<String> fetchAccessToken(String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", username);
        form.add("password", password);

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> {
                    JsonNode token = node.get("access_token");
                    if (token == null || token.asText().isBlank()) {
                        throw new KeycloakLoginException("Keycloak response missing access_token");
                    }
                    return token.asText();
                })
                .onErrorMap(ex -> {
                    if (ex instanceof KeycloakLoginException) {
                        return ex;
                    }
                    if (ex instanceof WebClientResponseException webEx) {
                        String body = webEx.getResponseBodyAsString();
                        String hint = body != null && !body.isBlank() ? body : webEx.getMessage();
                        return new KeycloakLoginException("Invalid username or password. " + hint);
                    }
                    return new KeycloakLoginException(
                            "Cannot reach Keycloak (is Docker running?). Start with: docker compose up -d — "
                                    + ex.getMessage());
                });
    }
}
