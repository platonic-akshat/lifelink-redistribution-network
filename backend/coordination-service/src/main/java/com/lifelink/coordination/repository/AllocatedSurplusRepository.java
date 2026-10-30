package com.lifelink.coordination.repository;

import com.lifelink.coordination.domain.AllocatedSurplus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AllocatedSurplusRepository extends JpaRepository<AllocatedSurplus, UUID> {
}
