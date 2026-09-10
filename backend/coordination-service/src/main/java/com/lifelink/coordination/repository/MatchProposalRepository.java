package com.lifelink.coordination.repository;

import com.lifelink.coordination.domain.MatchProposal;
import com.lifelink.coordination.domain.MatchProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatchProposalRepository extends JpaRepository<MatchProposal, UUID> {

    List<MatchProposal> findByStatus(MatchProposalStatus status);
}
