package com.lifelink.need.web;

import com.lifelink.need.domain.Need;
import com.lifelink.need.service.NeedService;
import com.lifelink.security.LifelinkPrincipalAccessor;
import com.lifelink.security.OrganizationVerificationGate;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/needs")
public class NeedController {

    private final NeedService service;
    private final LifelinkPrincipalAccessor principalAccessor;
    private final OrganizationVerificationGate verificationGate;

    public NeedController(NeedService service,
                          LifelinkPrincipalAccessor principalAccessor,
                          OrganizationVerificationGate verificationGate) {
        this.service = service;
        this.principalAccessor = principalAccessor;
        this.verificationGate = verificationGate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NeedResponse create(@Valid @RequestBody CreateNeedRequest request, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "NGO");
        UUID orgId = principalAccessor.requireOrganizationId(authentication);
        if (!request.requesterId().equals(orgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "requesterId must match your organization");
        }
        verificationGate.requireVerified(orgId, authentication);
        return NeedResponse.from(service.create(request));
    }

    @GetMapping("/{id}")
    public NeedResponse get(@PathVariable UUID id, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "NGO");
        Need need = service.getOrThrow(id);
        enforceOwnership(need, authentication);
        return NeedResponse.from(need);
    }

    @GetMapping
    public List<NeedResponse> listByRequester(@RequestParam UUID requesterId, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "NGO");
        UUID orgId = principalAccessor.requireOrganizationId(authentication);
        if (!requesterId.equals(orgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot list another organization's needs");
        }
        return service.listByRequester(requesterId).stream().map(NeedResponse::from).toList();
    }

    @PostMapping("/{id}/cancel")
    public NeedResponse cancel(@PathVariable UUID id, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "NGO");
        Need need = service.getOrThrow(id);
        enforceOwnership(need, authentication);
        return NeedResponse.from(service.cancel(id));
    }

    @PostMapping("/{id}/fulfill")
    public NeedResponse fulfill(@PathVariable UUID id, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "NGO");
        Need need = service.getOrThrow(id);
        enforceOwnership(need, authentication);
        return NeedResponse.from(service.markFulfilled(id));
    }

    private void enforceOwnership(Need need, Authentication authentication) {
        UUID orgId = principalAccessor.requireOrganizationId(authentication);
        if (!need.getRequesterId().equals(orgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot access another organization's need");
        }
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(jakarta.persistence.EntityNotFoundException ex) {
        return ex.getMessage();
    }
}
