package com.lifelink.coordination.web;

import com.lifelink.coordination.service.LogisticsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {

    private final LogisticsService service;

    public LogisticsController(LogisticsService service) {
        this.service = service;
    }

    @GetMapping("/{matchId}")
    public LogisticsResponse get(@PathVariable UUID matchId) {
        return LogisticsResponse.from(service.getOrThrow(matchId));
    }

    @PostMapping("/{matchId}/picked-up")
    public LogisticsResponse pickedUp(@PathVariable UUID matchId,
                                       @RequestBody(required = false) LogisticsUpdateRequest request) {
        return LogisticsResponse.from(service.markPickedUp(matchId, notes(request)));
    }

    @PostMapping("/{matchId}/delivered")
    public LogisticsResponse delivered(@PathVariable UUID matchId,
                                        @RequestBody(required = false) LogisticsUpdateRequest request) {
        return LogisticsResponse.from(service.markDelivered(matchId, notes(request)));
    }

    @PostMapping("/{matchId}/failed")
    public LogisticsResponse failed(@PathVariable UUID matchId,
                                     @RequestBody(required = false) LogisticsUpdateRequest request) {
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
