package com.lifelink.need.repository;

import com.lifelink.need.domain.InboundEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InboundEventRepository extends JpaRepository<InboundEvent, UUID> {
}
