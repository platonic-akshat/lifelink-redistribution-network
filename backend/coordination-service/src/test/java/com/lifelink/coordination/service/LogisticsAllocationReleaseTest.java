package com.lifelink.coordination.service;

import com.lifelink.coordination.domain.Logistics;
import com.lifelink.coordination.domain.MatchProposal;
import com.lifelink.coordination.domain.MatchProposalStatus;
import com.lifelink.coordination.messaging.LogisticsEventPublisher;
import com.lifelink.coordination.repository.AllocatedSurplusRepository;
import com.lifelink.coordination.repository.LogisticsRepository;
import com.lifelink.coordination.repository.MatchProposalRepository;
import com.lifelink.events.model.LogisticsStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogisticsAllocationReleaseTest {

    @Mock
    LogisticsRepository logisticsRepository;
    @Mock
    MatchProposalRepository matchProposalRepository;
    @Mock
    AllocatedSurplusRepository allocatedSurplusRepository;
    @Mock
    LogisticsEventPublisher eventPublisher;

    @InjectMocks
    LogisticsService logisticsService;

    @Test
    void markFailedReleasesAllocatedSurplusRows() {
        UUID matchId = UUID.randomUUID();
        UUID surplusId = UUID.randomUUID();
        Logistics logistics = new Logistics(matchId, LogisticsStatus.SCHEDULED, null, Instant.now());
        MatchProposal proposal = new MatchProposal(
                matchId,
                UUID.randomUUID(),
                List.of(surplusId),
                0.9,
                1.0,
                2.0,
                MatchProposalStatus.APPROVED,
                Instant.now());
        proposal.approve(UUID.randomUUID(), Instant.now());

        when(logisticsRepository.findById(matchId)).thenReturn(Optional.of(logistics));
        when(matchProposalRepository.findById(matchId)).thenReturn(Optional.of(proposal));

        logisticsService.markFailed(matchId, "driver unavailable");

        verify(allocatedSurplusRepository).deleteById(surplusId);
        verify(eventPublisher).publish(any(Logistics.class));
        assertThat(logistics.getStatus()).isEqualTo(LogisticsStatus.FAILED);
    }
}
