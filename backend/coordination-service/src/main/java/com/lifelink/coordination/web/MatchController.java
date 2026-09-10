package com.lifelink.coordination.web;

import com.lifelink.coordination.domain.MatchProposalStatus;
import com.lifelink.coordination.service.MatchService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService service;

    public MatchController(MatchService service) {
        this.service = service;
    }

    @GetMapping
    public List<MatchProposalResponse> list(
            @RequestParam(defaultValue = "PENDING") MatchProposalStatus status) {
        return service.listByStatus(status).stream().map(MatchProposalResponse::from).toList();
    }

    @PostMapping("/{matchId}/approve")
    public MatchProposalResponse approve(@PathVariable UUID matchId, @Valid @RequestBody ApproveRequest request) {
        return MatchProposalResponse.from(service.approve(matchId, request.decidedBy()));
    }

    @PostMapping("/{matchId}/reject")
    public MatchProposalResponse reject(@PathVariable UUID matchId, @Valid @RequestBody RejectRequest request) {
        return MatchProposalResponse.from(service.reject(matchId, request.decidedBy(), request.reason()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(EntityNotFoundException ex) {
        return ex.getMessage();
    }
}
