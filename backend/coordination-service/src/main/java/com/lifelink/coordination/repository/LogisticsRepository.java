package com.lifelink.coordination.repository;

import com.lifelink.coordination.domain.Logistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogisticsRepository extends JpaRepository<Logistics, UUID> {
}
