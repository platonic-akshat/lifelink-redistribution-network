package com.lifelink.gateway.web;

import com.lifelink.gateway.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Starter-grade login endpoint.
 *
 * There is no real user directory backing this: any non-blank
 * username/password is accepted and a JWT is issued for the requested role.
 * This exists purely so the gateway (and the two frontends) have something
 * to call end-to-end locally. In a real deployment this controller should be
 * deleted and replaced with delegation to a real identity provider (e.g.
 * Keycloak or Auth0) that the gateway validates tokens against.
 */
@RestController
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null
                || isBlank(request.username())
                || isBlank(request.password())
                || isBlank(request.role())) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("username, password, and role are required"));
        }

        String role = request.role().trim().toUpperCase();
        if (!role.equals("PROVIDER") && !role.equals("NGO")) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("role must be PROVIDER or NGO"));
        }

        String token = jwtService.issueToken(request.username().trim(), role);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ErrorResponse(String error) {
    }
}
