package com.lifelink.coordination.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class KeycloakRealmTest {

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:24.0.5")
            .withRealmImportFile("lifelink-realm.json");

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void realmExposesCoordinatorAndAdminRolesInAccessToken() throws Exception {
        assertRoles("coordinator1", "coordinator1", "COORDINATOR");
        assertRoles("admin1", "admin1", "ADMIN");
        assertRoles("ngo1", "ngo1", "NGO");
        assertRoles("provider1", "provider1", "PROVIDER");
    }

    private void assertRoles(String username, String password, String expectedRole) throws Exception {
        String token = fetchAccessToken(username, password);
        JsonNode payload = decodeJwtPayload(token);
        JsonNode roles = payload.path("realm_access").path("roles");
        assertThat(roles.toString()).contains(expectedRole);
    }

    private String fetchAccessToken(String username, String password) throws Exception {
        String form = "grant_type=password&client_id=lifelink-web&username="
                + username + "&password=" + password;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(keycloak.getAuthServerUrl()
                        + "/realms/lifelink/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body()).path("access_token").asText();
    }

    private JsonNode decodeJwtPayload(String jwt) throws Exception {
        String payload = jwt.split("\\.")[1];
        byte[] decoded = Base64.getUrlDecoder().decode(padBase64(payload));
        return objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));
    }

    private static String padBase64(String value) {
        int mod = value.length() % 4;
        if (mod == 0) {
            return value;
        }
        return value + "=".repeat(4 - mod);
    }
}
