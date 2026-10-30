package com.lifelink.coordination.web;

import com.lifelink.coordination.domain.Organization;
import com.lifelink.coordination.service.OrganizationService;
import com.lifelink.security.LifelinkPrincipalAccessor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService service;
    private final LifelinkPrincipalAccessor principalAccessor;

    public OrganizationController(OrganizationService service, LifelinkPrincipalAccessor principalAccessor) {
        this.service = service;
        this.principalAccessor = principalAccessor;
    }

    @GetMapping("/{organizationId}/verification")
    public OrganizationVerificationResponse verification(@PathVariable UUID organizationId,
                                                          Authentication authentication) {
        allowRead(organizationId, authentication);
        Organization organization = service.getOrThrow(organizationId);
        return OrganizationVerificationResponse.from(organization);
    }

    private void allowRead(UUID organizationId, Authentication authentication) {
        try {
            principalAccessor.requireAnyRole(authentication, "ADMIN");
            return;
        } catch (org.springframework.security.access.AccessDeniedException ignored) {
            // fall through
        }
        UUID orgId = principalAccessor.requireOrganizationId(authentication);
        if (!orgId.equals(organizationId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot read another organization's verification");
        }
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(EntityNotFoundException ex) {
        return ex.getMessage();
    }

    public record OrganizationVerificationResponse(UUID organizationId, String verificationStatus) {
        static OrganizationVerificationResponse from(Organization organization) {
            return new OrganizationVerificationResponse(
                    organization.getId(),
                    organization.getVerificationStatus().name());
        }
    }
}
