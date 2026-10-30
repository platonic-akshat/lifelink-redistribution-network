package com.lifelink.coordination.service;

import com.lifelink.coordination.domain.MatchProposal;
import com.lifelink.coordination.domain.MatchProposalStatus;
import com.lifelink.coordination.messaging.LogisticsEventPublisher;
import com.lifelink.coordination.messaging.MatchDecisionEventPublisher;
import com.lifelink.coordination.repository.AllocatedSurplusRepository;
import com.lifelink.coordination.repository.LogisticsRepository;
import com.lifelink.coordination.repository.MatchProposalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
@ActiveProfiles("integration-test")
class ConcurrentMatchApprovalTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("coordination_db")
            .withUsername("lifelink")
            .withPassword("lifelink");

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }

    @Autowired
    MatchService matchService;

    @Autowired
    MatchProposalRepository proposalRepository;

    @Autowired
    AllocatedSurplusRepository allocatedSurplusRepository;

    @Autowired
    LogisticsRepository logisticsRepository;

    @MockBean
    MatchDecisionEventPublisher decisionEventPublisher;

    @MockBean
    LogisticsEventPublisher logisticsEventPublisher;

    @Test
    void concurrentApprovalsOnSameSurplusAllowOnlyOneAllocation() throws Exception {
        UUID surplusId = UUID.randomUUID();
        UUID needA = UUID.randomUUID();
        UUID needB = UUID.randomUUID();
        UUID matchA = UUID.randomUUID();
        UUID matchB = UUID.randomUUID();
        UUID coordinator = UUID.randomUUID();

        proposalRepository.save(new MatchProposal(
                matchA, needA, List.of(surplusId), 9.0, 1.0, 5.0,
                MatchProposalStatus.PENDING, Instant.now()));
        proposalRepository.save(new MatchProposal(
                matchB, needB, List.of(surplusId), 8.0, 2.0, 4.0,
                MatchProposalStatus.PENDING, Instant.now()));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();

        Future<?> f1 = pool.submit(() -> runApprove(matchA, coordinator, start, successes, conflicts));
        Future<?> f2 = pool.submit(() -> runApprove(matchB, coordinator, start, successes, conflicts));

        start.countDown();
        f1.get();
        f2.get();
        pool.shutdownNow();

        assertThat(successes.get()).isEqualTo(1);
        assertThat(conflicts.get()).isEqualTo(1);
        assertThat(allocatedSurplusRepository.count()).isEqualTo(1);
        assertThat(logisticsRepository.count()).isEqualTo(1);

        long approved = proposalRepository.findAll().stream()
                .filter(p -> p.getStatus() == MatchProposalStatus.APPROVED)
                .count();
        assertThat(approved).isEqualTo(1);
    }

    private void runApprove(UUID matchId, UUID coordinator, CountDownLatch start,
                            AtomicInteger successes, AtomicInteger conflicts) {
        try {
            start.await();
            matchService.approve(matchId, coordinator);
            successes.incrementAndGet();
        } catch (IllegalStateException ex) {
            conflicts.incrementAndGet();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void secondApprovalAfterFirstAllocationFails() {
        UUID surplusId = UUID.randomUUID();
        UUID matchA = UUID.randomUUID();
        UUID matchB = UUID.randomUUID();
        UUID coordinator = UUID.randomUUID();

        proposalRepository.save(new MatchProposal(
                matchA, UUID.randomUUID(), List.of(surplusId), 9.0, 1.0, 5.0,
                MatchProposalStatus.PENDING, Instant.now()));
        proposalRepository.save(new MatchProposal(
                matchB, UUID.randomUUID(), List.of(surplusId), 8.0, 2.0, 4.0,
                MatchProposalStatus.PENDING, Instant.now()));

        matchService.approve(matchA, coordinator);
        assertThrows(IllegalStateException.class, () -> matchService.approve(matchB, coordinator));
        assertThat(allocatedSurplusRepository.count()).isEqualTo(1);
    }
}
