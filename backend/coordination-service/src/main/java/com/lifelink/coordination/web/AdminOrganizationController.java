package com.lifelink.coordination.web;

import com.lifelink.coordination.domain.Organization;
import com.lifelink.coordination.domain.OrganizationVerificationStatus;
import com.lifelink.coordination.service.OrganizationService;
import com.lifelink.security.LifelinkPrincipalAccessor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/organizations")
public class AdminOrganizationController {

    private final OrganizationService service;
    private final LifelinkPrincipalAccessor principalAccessor;

    public AdminOrganizationController(OrganizationService service, LifelinkPrincipalAccessor principalAccessor) {
        this.service = service;
        this.principalAccessor = principalAccessor;
    }

    @PatchMapping("/{organizationId}/verification")
    public OrganizationController.OrganizationVerificationResponse updateVerification(
            @PathVariable UUID organizationId,
            @Valid @RequestBody UpdateVerificationRequest request,
            Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "ADMIN");
        Organization organization = service.updateVerification(organizationId, request.status());
        return OrganizationController.OrganizationVerificationResponse.from(organization);
    }

    public record UpdateVerificationRequest(@NotNull OrganizationVerificationStatus status) {
    }
}
