package com.lifelink.surplus.web;

import com.lifelink.surplus.domain.Surplus;
import com.lifelink.surplus.service.SurplusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/surplus")
public class SurplusController {

    private final SurplusService service;

    public SurplusController(SurplusService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SurplusResponse create(@Valid @RequestBody CreateSurplusRequest request) {
        return SurplusResponse.from(service.create(request));
    }

    @GetMapping("/{id}")
    public SurplusResponse get(@PathVariable UUID id) {
        return SurplusResponse.from(service.getOrThrow(id));
    }

    @GetMapping
    public List<SurplusResponse> listByProvider(@RequestParam UUID providerId) {
        return service.listByProvider(providerId).stream().map(SurplusResponse::from).toList();
    }

    @GetMapping("/nearby")
    public List<SurplusResponse> nearby(@RequestParam double lat,
                                         @RequestParam double lon,
                                         @RequestParam(defaultValue = "10") double radiusKm) {
        return service.listNearby(lat, lon, radiusKm).stream().map(SurplusResponse::from).toList();
    }

    @PostMapping("/{id}/claim")
    public SurplusResponse claim(@PathVariable UUID id) {
        return SurplusResponse.from(service.claim(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdraw(@PathVariable UUID id) {
        service.withdraw(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(jakarta.persistence.EntityNotFoundException ex) {
        return ex.getMessage();
    }
}
