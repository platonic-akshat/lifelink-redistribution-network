package com.lifelink.coordination.web;

import com.lifelink.coordination.domain.MatchProposalStatus;
import com.lifelink.coordination.service.MatchService;
import com.lifelink.security.LifelinkPrincipalAccessor;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService service;
    private final LifelinkPrincipalAccessor principalAccessor;

    public MatchController(MatchService service, LifelinkPrincipalAccessor principalAccessor) {
        this.service = service;
        this.principalAccessor = principalAccessor;
    }

    @GetMapping
    public List<MatchProposalResponse> list(
            @RequestParam(defaultValue = "PENDING") MatchProposalStatus status,
            Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "COORDINATOR", "ADMIN");
        return service.listByStatus(status).stream().map(MatchProposalResponse::from).toList();
    }

    @PostMapping("/{matchId}/approve")
    public MatchProposalResponse approve(@PathVariable UUID matchId,
                                          @Valid @RequestBody ApproveRequest request,
                                          Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "COORDINATOR", "ADMIN");
        enforceDecidedBy(request.decidedBy(), authentication);
        return MatchProposalResponse.from(service.approve(matchId, request.decidedBy()));
    }

    @PostMapping("/{matchId}/reject")
    public MatchProposalResponse reject(@PathVariable UUID matchId,
                                         @Valid @RequestBody RejectRequest request,
                                         Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "COORDINATOR", "ADMIN");
        enforceDecidedBy(request.decidedBy(), authentication);
        return MatchProposalResponse.from(service.reject(matchId, request.decidedBy(), request.reason()));
    }

    private void enforceDecidedBy(UUID decidedBy, Authentication authentication) {
        UUID subject = principalAccessor.requireSubjectAsUuid(authentication);
        if (!subject.equals(decidedBy)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "decidedBy must match the authenticated user");
        }
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(EntityNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(IllegalStateException ex) {
        return ex.getMessage();
    }
}
