package com.lifelink.surplus.service;

import com.lifelink.events.model.SurplusStatus;
import com.lifelink.surplus.domain.Surplus;
import com.lifelink.surplus.messaging.SurplusEventPublisher;
import com.lifelink.surplus.repository.SurplusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Periodically flags surplus nearing expiry so matching-service can boost
 * its urgency score, and expires postings past their expiry timestamp so
 * they stop showing up in nearby/matching queries.
 */
@Component
public class SurplusExpirySweepJob {

    private final SurplusRepository repository;
    private final SurplusEventPublisher eventPublisher;
    private final long expiringSoonWindowHours;

    public SurplusExpirySweepJob(SurplusRepository repository,
                                  SurplusEventPublisher eventPublisher,
                                  @Value("${lifelink.surplus.expiring-soon-window-hours:6}") long expiringSoonWindowHours) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.expiringSoonWindowHours = expiringSoonWindowHours;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void sweep() {
        Instant now = Instant.now();

        List<Surplus> expired = repository.findByStatusAndExpiresAtBefore(SurplusStatus.POSTED, now);
        expired.addAll(repository.findByStatusAndExpiresAtBefore(SurplusStatus.EXPIRING_SOON, now));
        for (Surplus s : expired) {
            s.setStatus(SurplusStatus.EXPIRED);
            s.setUpdatedAt(now);
            eventPublisher.publish(s);
        }

        Instant expiringSoonThreshold = now.plusSeconds(expiringSoonWindowHours * 3600);
        List<Surplus> expiringSoon = repository.findByStatusAndExpiresAtBefore(SurplusStatus.POSTED, expiringSoonThreshold);
        for (Surplus s : expiringSoon) {
            s.setStatus(SurplusStatus.EXPIRING_SOON);
            s.setUpdatedAt(now);
            eventPublisher.publish(s);
        }
    }
}
