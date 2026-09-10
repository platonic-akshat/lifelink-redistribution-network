package com.lifelink.need.repository;

import com.lifelink.need.domain.Need;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NeedRepository extends JpaRepository<Need, UUID> {

    List<Need> findByRequesterId(UUID requesterId);
}
