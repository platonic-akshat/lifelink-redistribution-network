package com.lifelink.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Reads LifeLink identity claims from a Keycloak-issued JWT.
 *
 * Expected token shape (dev realm):
 * - {@code organization_id}: UUID string for the caller's organization
 * - {@code realm_access.roles}: PROVIDER, NGO, … (mapped to ROLE_* authorities)
 */
public class LifelinkPrincipalAccessor {

    public UUID requireOrganizationId(Authentication authentication) {
        Jwt jwt = requireJwt(authentication);
        String raw = jwt.getClaimAsString("organization_id");
        if (raw == null || raw.isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException("Missing organization_id claim");
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.security.access.AccessDeniedException("Invalid organization_id claim");
        }
    }

    public UUID organizationIdOrNull(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            return null;
        }
        String raw = ((JwtAuthenticationToken) authentication).getToken().getClaimAsString("organization_id");
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public void requireAnyRole(Authentication authentication, String... roles) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (String role : roles) {
            String expected = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals(expected)) {
                    return;
                }
            }
        }
        throw new org.springframework.security.access.AccessDeniedException("Insufficient role");
    }

    public UUID requireSubjectAsUuid(Authentication authentication) {
        Jwt jwt = requireJwt(authentication);
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.security.access.AccessDeniedException("Subject must be a UUID for decidedBy");
        }
    }

    private static Jwt requireJwt(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new org.springframework.security.access.AccessDeniedException("JWT authentication required");
        }
        return jwtAuth.getToken();
    }

    @SuppressWarnings("unchecked")
    public static boolean hasRealmRole(Jwt jwt, String role) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return false;
        }
        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) {
            return false;
        }
        return roles.contains(role);
    }
}
