package com.lifelink.need.service;

import com.lifelink.events.geo.GeoPoint;
import com.lifelink.events.geo.GeoUtil;
import com.lifelink.events.model.NeedStatus;
import com.lifelink.need.domain.Need;
import com.lifelink.need.messaging.NeedEventPublisher;
import com.lifelink.need.repository.NeedRepository;
import com.lifelink.need.web.CreateNeedRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NeedService {

    private static final int GEOHASH_PRECISION = 5;

    private final NeedRepository repository;
    private final NeedEventPublisher eventPublisher;

    public NeedService(NeedRepository repository, NeedEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Need create(CreateNeedRequest request) {
        Instant now = Instant.now();
        GeoPoint location = new GeoPoint(request.latitude(), request.longitude());
        Need need = new Need(
                UUID.randomUUID(),
                request.category(),
                request.subType(),
                request.quantity(),
                request.unit(),
                request.urgency(),
                request.latitude(),
                request.longitude(),
                GeoUtil.geohash(location, GEOHASH_PRECISION),
                request.requesterId(),
                request.requesterName(),
                NeedStatus.POSTED,
                now,
                now
        );
        repository.save(need);
        eventPublisher.publish(need);
        return need;
    }

    public Need getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Need not found: " + id));
    }

    public List<Need> listByRequester(UUID requesterId) {
        return repository.findByRequesterId(requesterId);
    }

    @Transactional
    public Need cancel(UUID id) {
        Need need = getOrThrow(id);
        need.setStatus(NeedStatus.CANCELLED);
        need.setUpdatedAt(Instant.now());
        eventPublisher.publish(need);
        return need;
    }

    @Transactional
    public Need markFulfilled(UUID id) {
        Need need = getOrThrow(id);
        need.setStatus(NeedStatus.FULFILLED);
        need.setUpdatedAt(Instant.now());
        eventPublisher.publish(need);
        return need;
    }
}
