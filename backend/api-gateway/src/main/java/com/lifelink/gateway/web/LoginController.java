package com.lifelink.gateway.web;

import com.lifelink.gateway.auth.KeycloakLoginException;
import com.lifelink.gateway.auth.KeycloakPasswordGrantClient;
import com.lifelink.gateway.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Single login entry for frontends. Proxies the password grant to Keycloak server-side
 * (avoids browser CORS issues). Optional legacy stub when {@code lifelink.auth.legacy-login-enabled=true}.
 */
@RestController
public class LoginController {

    private final KeycloakPasswordGrantClient keycloakClient;
    private final JwtService jwtService;
    private final boolean legacyLoginEnabled;

    public LoginController(KeycloakPasswordGrantClient keycloakClient,
                           JwtService jwtService,
                           @Value("${lifelink.auth.legacy-login-enabled:false}") boolean legacyLoginEnabled) {
        this.keycloakClient = keycloakClient;
        this.jwtService = jwtService;
        this.legacyLoginEnabled = legacyLoginEnabled;
    }

    @PostMapping("/api/auth/login")
    public Mono<ResponseEntity<?>> login(@RequestBody LoginRequest request) {
        if (request == null
                || isBlank(request.username())
                || isBlank(request.password())
                || isBlank(request.role())) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(new ErrorResponse("username, password, and role are required")));
        }

        if (legacyLoginEnabled) {
            return Mono.just(legacyLogin(request));
        }

        String keycloakUser = resolveKeycloakUsername(request.role().trim(), request.username().trim());
        return keycloakClient.fetchAccessToken(keycloakUser, request.password().trim())
                .map(token -> ResponseEntity.ok(new LoginResponse(token)));
    }

    private ResponseEntity<?> legacyLogin(LoginRequest request) {
        String role = request.role().trim().toUpperCase();
        if (!role.equals("PROVIDER") && !role.equals("NGO") && !role.equals("COORDINATOR")) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("role must be PROVIDER, NGO, or COORDINATOR"));
        }
        String token = jwtService.issueToken(request.username().trim(), role);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    static String resolveKeycloakUsername(String role, String username) {
        String lower = username.toLowerCase();
        if (lower.contains("provider") || lower.contains("ngo") || lower.contains("coordinator")
                || lower.contains("admin")) {
            return username;
        }
        return switch (role.toUpperCase()) {
            case "PROVIDER" -> "provider1";
            case "COORDINATOR" -> "coordinator1";
            default -> "ngo1";
        };
    }

    @ExceptionHandler(KeycloakLoginException.class)
    public ResponseEntity<ErrorResponse> handleKeycloak(KeycloakLoginException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ErrorResponse(String error) {
    }
}
