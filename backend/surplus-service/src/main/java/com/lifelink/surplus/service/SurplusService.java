package com.lifelink.surplus.service;

import com.lifelink.events.geo.GeoPoint;
import com.lifelink.events.geo.GeoUtil;
import com.lifelink.events.model.SurplusStatus;
import com.lifelink.surplus.domain.Surplus;
import com.lifelink.surplus.messaging.SurplusEventPublisher;
import com.lifelink.surplus.repository.SurplusRepository;
import com.lifelink.surplus.web.CreateSurplusRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SurplusService {

    private static final int GEOHASH_PRECISION = 5;

    private final SurplusRepository repository;
    private final SurplusEventPublisher eventPublisher;

    public SurplusService(SurplusRepository repository, SurplusEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Surplus create(CreateSurplusRequest request) {
        Instant now = Instant.now();
        GeoPoint location = new GeoPoint(request.latitude(), request.longitude());
        Surplus surplus = new Surplus(
                UUID.randomUUID(),
                request.category(),
                request.subType(),
                request.quantity(),
                request.unit(),
                request.latitude(),
                request.longitude(),
                GeoUtil.geohash(location, GEOHASH_PRECISION),
                request.expiresAt(),
                request.providerId(),
                request.providerName(),
                SurplusStatus.POSTED,
                now,
                now
        );
        repository.save(surplus);
        // NOTE: publish-after-commit here is "at least once, usually" — a
        // production hardening step is the transactional outbox pattern
        // (write event to an outbox table in the same DB transaction, relay
        // via Debezium/CDC) so a crash between save() and publish() can't
        // silently drop the event. See ARCHITECTURE.md §7.
        eventPublisher.publish(surplus);
        return surplus;
    }

    public Surplus getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Surplus not found: " + id));
    }

    public List<Surplus> listByProvider(UUID providerId) {
        return repository.findByProviderId(providerId);
    }

    /** Coarse nearby lookup: candidate geohash cell + its 8 neighbors, then exact haversine filter. */
    public List<Surplus> listNearby(double lat, double lon, double radiusKm) {
        GeoPoint origin = new GeoPoint(lat, lon);
        List<String> candidateCells = GeoUtil.neighboringCells(origin, GEOHASH_PRECISION);
        return repository.findByGeohash5InAndStatus(candidateCells, SurplusStatus.POSTED).stream()
                .filter(s -> GeoUtil.distanceKm(origin, new GeoPoint(s.getLatitude(), s.getLongitude())) <= radiusKm)
                .toList();
    }

    @Transactional
    public Surplus claim(UUID id) {
        Surplus surplus = getOrThrow(id);
        surplus.setStatus(SurplusStatus.CLAIMED);
        surplus.setUpdatedAt(Instant.now());
        eventPublisher.publish(surplus);
        return surplus;
    }

    @Transactional
    public Surplus withdraw(UUID id) {
        Surplus surplus = getOrThrow(id);
        surplus.setStatus(SurplusStatus.EXPIRED);
        surplus.setUpdatedAt(Instant.now());
        eventPublisher.publish(surplus);
        return surplus;
    }

    @Transactional
    public void applyMatchApproved(UUID surplusId) {
        Surplus surplus = getOrThrow(surplusId);
        if (surplus.getStatus() == SurplusStatus.CLAIMED
                || surplus.getStatus() == SurplusStatus.COMPLETED) {
            return;
        }
        if (surplus.getStatus() != SurplusStatus.POSTED
                && surplus.getStatus() != SurplusStatus.EXPIRING_SOON) {
            throw new IllegalStateException("Surplus " + surplusId + " is not available for matching");
        }
        surplus.setStatus(SurplusStatus.CLAIMED);
        surplus.setUpdatedAt(Instant.now());
        eventPublisher.publish(surplus);
    }

    @Transactional
    public void applyLogisticsDelivered(UUID surplusId) {
        Surplus surplus = getOrThrow(surplusId);
        if (surplus.getStatus() == SurplusStatus.COMPLETED) {
            return;
        }
        if (surplus.getStatus() != SurplusStatus.CLAIMED) {
            throw new IllegalStateException("Surplus " + surplusId + " must be claimed before delivery");
        }
        surplus.setStatus(SurplusStatus.COMPLETED);
        surplus.setUpdatedAt(Instant.now());
        eventPublisher.publish(surplus);
    }

    @Transactional
    public void applyLogisticsFailed(UUID surplusId) {
        Surplus surplus = getOrThrow(surplusId);
        if (surplus.getStatus() == SurplusStatus.POSTED) {
            return;
        }
        if (surplus.getStatus() == SurplusStatus.CLAIMED) {
            surplus.setStatus(SurplusStatus.POSTED);
            surplus.setUpdatedAt(Instant.now());
            eventPublisher.publish(surplus);
        }
    }
}
