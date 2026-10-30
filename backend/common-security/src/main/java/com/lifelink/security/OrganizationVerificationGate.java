package com.lifelink.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

/**
 * Calls coordination-service to ensure an organization is verified before posting surplus/needs.
 */
@Component
public class OrganizationVerificationGate {

    private final RestTemplate restTemplate;
    private final String coordinationBaseUrl;
    private final boolean enforce;

    public OrganizationVerificationGate(RestTemplate restTemplate,
                                        @Value("${lifelink.coordination.base-url:http://localhost:8083}") String coordinationBaseUrl,
                                        @Value("${lifelink.verification.enforce:true}") boolean enforce) {
        this.restTemplate = restTemplate;
        this.coordinationBaseUrl = coordinationBaseUrl;
        this.enforce = enforce;
    }

    public void requireVerified(UUID organizationId, Authentication authentication) {
        if (!enforce) {
            return;
        }
        String bearer = bearerToken(authentication);
        String url = coordinationBaseUrl + "/api/organizations/" + organizationId + "/verification";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearer);
        try {
            ResponseEntity<VerificationResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), VerificationResponse.class);
            VerificationResponse body = response.getBody();
            if (body == null || !"VERIFIED".equals(body.verificationStatus())) {
                throw new ResponseStatusException(FORBIDDEN, "Organization is not verified for posting");
            }
        } catch (RestClientException ex) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Unable to verify organization status");
        }
    }

    private static String bearerToken(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new org.springframework.security.access.AccessDeniedException("JWT authentication required");
        }
        return jwtAuth.getToken().getTokenValue();
    }

    public record VerificationResponse(UUID organizationId, String verificationStatus) {
    }
}
