package com.lifelink.need.web;

import com.lifelink.need.service.NeedService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/needs")
public class NeedController {

    private final NeedService service;

    public NeedController(NeedService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NeedResponse create(@Valid @RequestBody CreateNeedRequest request) {
        return NeedResponse.from(service.create(request));
    }

    @GetMapping("/{id}")
    public NeedResponse get(@PathVariable UUID id) {
        return NeedResponse.from(service.getOrThrow(id));
    }

    @GetMapping
    public List<NeedResponse> listByRequester(@RequestParam UUID requesterId) {
        return service.listByRequester(requesterId).stream().map(NeedResponse::from).toList();
    }

    @PostMapping("/{id}/cancel")
    public NeedResponse cancel(@PathVariable UUID id) {
        return NeedResponse.from(service.cancel(id));
    }

    @PostMapping("/{id}/fulfill")
    public NeedResponse fulfill(@PathVariable UUID id) {
        return NeedResponse.from(service.markFulfilled(id));
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(jakarta.persistence.EntityNotFoundException ex) {
        return ex.getMessage();
    }
}
