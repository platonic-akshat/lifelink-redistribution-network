package com.lifelink.coordination.repository;

import com.lifelink.coordination.domain.Logistics;
import com.lifelink.events.model.LogisticsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogisticsRepository extends JpaRepository<Logistics, UUID> {

    Page<Logistics> findByStatus(LogisticsStatus status, Pageable pageable);

    Page<Logistics> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
