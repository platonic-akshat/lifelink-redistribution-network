package com.lifelink.surplus.web;

import com.lifelink.security.LifelinkPrincipalAccessor;
import com.lifelink.security.OrganizationVerificationGate;
import com.lifelink.surplus.domain.Surplus;
import com.lifelink.surplus.service.SurplusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/surplus")
public class SurplusController {

    private final SurplusService service;
    private final LifelinkPrincipalAccessor principalAccessor;
    private final OrganizationVerificationGate verificationGate;

    public SurplusController(SurplusService service,
                             LifelinkPrincipalAccessor principalAccessor,
                             OrganizationVerificationGate verificationGate) {
        this.service = service;
        this.principalAccessor = principalAccessor;
        this.verificationGate = verificationGate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SurplusResponse create(@Valid @RequestBody CreateSurplusRequest request, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "PROVIDER");
        UUID orgId = principalAccessor.requireOrganizationId(authentication);
        if (!request.providerId().equals(orgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "providerId must match your organization");
        }
        verificationGate.requireVerified(orgId, authentication);
        return SurplusResponse.from(service.create(request));
    }

    @GetMapping("/{id}")
    public SurplusResponse get(@PathVariable UUID id, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "PROVIDER", "NGO");
        Surplus surplus = service.getOrThrow(id);
        UUID orgId = principalAccessor.organizationIdOrNull(authentication);
        if (orgId != null && surplus.getProviderId().equals(orgId)) {
            return SurplusResponse.from(surplus);
        }
        // NGOs may read surplus metadata for operational workflows; providers cannot read others' listings.
        try {
            principalAccessor.requireAnyRole(authentication, "NGO");
            return SurplusResponse.from(surplus);
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot access another organization's surplus");
        }
    }

    @GetMapping
    public List<SurplusResponse> listByProvider(@RequestParam UUID providerId, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "PROVIDER");
        UUID orgId = principalAccessor.requireOrganizationId(authentication);
        if (!providerId.equals(orgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot list another organization's surplus");
        }
        return service.listByProvider(providerId).stream().map(SurplusResponse::from).toList();
    }

    @GetMapping("/nearby")
    public List<SurplusResponse> nearby(@RequestParam double lat,
                                         @RequestParam double lon,
                                         @RequestParam(defaultValue = "10") double radiusKm,
                                         Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "PROVIDER", "NGO");
        return service.listNearby(lat, lon, radiusKm).stream().map(SurplusResponse::from).toList();
    }

    @PostMapping("/{id}/claim")
    public SurplusResponse claim(@PathVariable UUID id, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "NGO");
        return SurplusResponse.from(service.claim(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdraw(@PathVariable UUID id, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "PROVIDER");
        Surplus surplus = service.getOrThrow(id);
        UUID orgId = principalAccessor.requireOrganizationId(authentication);
        if (!surplus.getProviderId().equals(orgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot withdraw another organization's surplus");
        }
        service.withdraw(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(jakarta.persistence.EntityNotFoundException ex) {
        return ex.getMessage();
    }
}
