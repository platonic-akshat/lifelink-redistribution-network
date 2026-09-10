package com.lifelink.surplus.repository;

import com.lifelink.events.model.SurplusStatus;
import com.lifelink.surplus.domain.Surplus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SurplusRepository extends JpaRepository<Surplus, UUID> {

    List<Surplus> findByProviderId(UUID providerId);

    List<Surplus> findByGeohash5InAndStatus(Collection<String> geohash5Buckets, SurplusStatus status);

    List<Surplus> findByStatusAndExpiresAtBefore(SurplusStatus status, Instant threshold);
}
