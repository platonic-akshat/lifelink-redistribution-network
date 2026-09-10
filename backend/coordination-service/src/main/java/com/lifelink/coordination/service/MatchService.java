package com.lifelink.coordination.service;

import com.lifelink.coordination.domain.Logistics;
import com.lifelink.coordination.domain.MatchProposal;
import com.lifelink.coordination.domain.MatchProposalStatus;
import com.lifelink.coordination.messaging.LogisticsEventPublisher;
import com.lifelink.coordination.messaging.MatchDecisionEventPublisher;
import com.lifelink.coordination.repository.LogisticsRepository;
import com.lifelink.coordination.repository.MatchProposalRepository;
import com.lifelink.events.model.LogisticsStatus;
import com.lifelink.events.model.MatchDecision;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MatchService {

    private final MatchProposalRepository proposalRepository;
    private final LogisticsRepository logisticsRepository;
    private final MatchDecisionEventPublisher decisionEventPublisher;
    private final LogisticsEventPublisher logisticsEventPublisher;

    public MatchService(MatchProposalRepository proposalRepository,
                         LogisticsRepository logisticsRepository,
                         MatchDecisionEventPublisher decisionEventPublisher,
                         LogisticsEventPublisher logisticsEventPublisher) {
        this.proposalRepository = proposalRepository;
        this.logisticsRepository = logisticsRepository;
        this.decisionEventPublisher = decisionEventPublisher;
        this.logisticsEventPublisher = logisticsEventPublisher;
    }

    public List<MatchProposal> listByStatus(MatchProposalStatus status) {
        return proposalRepository.findByStatus(status);
    }

    public MatchProposal getOrThrow(UUID matchId) {
        return proposalRepository.findById(matchId)
                .orElseThrow(() -> new EntityNotFoundException("Match proposal not found: " + matchId));
    }

    @Transactional
    public MatchProposal approve(UUID matchId, UUID decidedBy) {
        MatchProposal proposal = getOrThrow(matchId);
        proposal.approve(decidedBy, Instant.now());
        decisionEventPublisher.publish(proposal, MatchDecision.APPROVED);

        Logistics logistics = new Logistics(matchId, LogisticsStatus.SCHEDULED, null, Instant.now());
        logisticsRepository.save(logistics);
        logisticsEventPublisher.publish(logistics);

        return proposal;
    }

    @Transactional
    public MatchProposal reject(UUID matchId, UUID decidedBy, String reason) {
        MatchProposal proposal = getOrThrow(matchId);
        proposal.reject(decidedBy, reason, Instant.now());
        decisionEventPublisher.publish(proposal, MatchDecision.REJECTED);
        return proposal;
    }
}
