package com.lifelink.coordination.web;

import com.lifelink.coordination.service.DltReplayService;
import com.lifelink.outbox.OutboxEvent;
import com.lifelink.outbox.OutboxEventRepository;
import com.lifelink.outbox.OutboxService;
import com.lifelink.outbox.OutboxStatus;
import com.lifelink.security.LifelinkPrincipalAccessor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminMessagingController {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxService outboxService;
    private final DltReplayService dltReplayService;
    private final LifelinkPrincipalAccessor principalAccessor;

    public AdminMessagingController(OutboxEventRepository outboxEventRepository,
                                    OutboxService outboxService,
                                    DltReplayService dltReplayService,
                                    LifelinkPrincipalAccessor principalAccessor) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxService = outboxService;
        this.dltReplayService = dltReplayService;
        this.principalAccessor = principalAccessor;
    }

    @GetMapping("/outbox")
    public Page<OutboxEventResponse> listOutbox(@RequestParam(defaultValue = "FAILED") OutboxStatus status,
                                                 Pageable pageable,
                                                 Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "ADMIN");
        return outboxEventRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(OutboxEventResponse::from);
    }

    @PostMapping("/outbox/{eventId}/requeue")
    public OutboxEventResponse requeueOutbox(@PathVariable UUID eventId, Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "ADMIN");
        outboxService.requeue(eventId);
        OutboxEvent event = outboxEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Outbox event not found: " + eventId));
        return OutboxEventResponse.from(event);
    }

    @PostMapping("/dlt/replay")
    public DltReplayService.ReplayResult replayDlt(@Valid @RequestBody ReplayDltRequest request,
                                                    Authentication authentication) {
        principalAccessor.requireAnyRole(authentication, "ADMIN");
        return dltReplayService.replay(request.dltTopic(), request.partition(), request.offset());
    }

    public record ReplayDltRequest(
            @NotBlank String dltTopic,
            @NotNull Integer partition,
            @NotNull Long offset) {
    }

    public record OutboxEventResponse(
            UUID eventId,
            String topic,
            String messageKey,
            OutboxStatus status,
            int publishAttempts,
            String lastError) {
        static OutboxEventResponse from(OutboxEvent event) {
            return new OutboxEventResponse(
                    event.getEventId(),
                    event.getTopic(),
                    event.getMessageKey(),
                    event.getStatus(),
                    event.getPublishAttempts(),
                    event.getLastError());
        }
    }
}
