package com.lifelink.surplus.repository;

import com.lifelink.surplus.domain.InboundEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InboundEventRepository extends JpaRepository<InboundEvent, UUID> {
}
