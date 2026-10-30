package com.lifelink.coordination.web;

import com.lifelink.coordination.service.LogisticsService;
import com.lifelink.events.model.LogisticsStatus;
import com.lifelink.security.LifelinkPrincipalAccessor;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    private final LogisticsService service;
    private final LifelinkPrincipalAccessor principalAccessor;

    public LogisticsController(LogisticsService service, LifelinkPrincipalAccessor principalAccessor) {
        this.service = service;
        this.principalAccessor = principalAccessor;
    }

    @GetMapping
    public Page<LogisticsResponse> list(
            @RequestParam(required = false) LogisticsStatus status,
            Pageable pageable,
            Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "COORDINATOR", "ADMIN");
        return service.list(status, pageable).map(LogisticsResponse::from);
    }

    @GetMapping("/{matchId}")
    public LogisticsResponse get(@PathVariable UUID matchId, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "COORDINATOR", "ADMIN");
        return LogisticsResponse.from(service.getOrThrow(matchId));
    }

    @PostMapping("/{matchId}/picked-up")
    public LogisticsResponse pickedUp(@PathVariable UUID matchId,
                                       @RequestBody(required = false) LogisticsUpdateRequest request,
                                       Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "COORDINATOR", "ADMIN");
        return LogisticsResponse.from(service.markPickedUp(matchId, notes(request)));
    }

    @PostMapping("/{matchId}/delivered")
    public LogisticsResponse delivered(@PathVariable UUID matchId,
                                        @RequestBody(required = false) LogisticsUpdateRequest request,
                                        Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "COORDINATOR", "ADMIN");
        return LogisticsResponse.from(service.markDelivered(matchId, notes(request)));
    }

    @PostMapping("/{matchId}/failed")
    public LogisticsResponse failed(@PathVariable UUID matchId,
                                     @RequestBody(required = false) LogisticsUpdateRequest request,
                                     Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "COORDINATOR", "ADMIN");
        return LogisticsResponse.from(service.markFailed(matchId, notes(request)));
    }

    private static String notes(LogisticsUpdateRequest request) {
        return request != null ? request.notes() : null;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(EntityNotFoundException ex) {
        return ex.getMessage();
    }
}
