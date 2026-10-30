package com.lifelink.outbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = OutboxTestApplication.class)
@EntityScan(basePackageClasses = OutboxEvent.class)
@EnableJpaRepositories(basePackageClasses = OutboxEventRepository.class)
@Import(OutboxService.class)
@Sql(statements = {
        "CREATE TABLE IF NOT EXISTS outbox_event ("
                + "event_id UUID PRIMARY KEY, "
                + "aggregate_type VARCHAR(50) NOT NULL, "
                + "aggregate_id UUID NOT NULL, "
                + "event_type VARCHAR(100) NOT NULL, "
                + "topic VARCHAR(100) NOT NULL, "
                + "message_key VARCHAR(200) NOT NULL, "
                + "payload TEXT NOT NULL, "
                + "schema_version INT NOT NULL, "
                + "status VARCHAR(20) NOT NULL, "
                + "publish_attempts INT NOT NULL DEFAULT 0, "
                + "last_error VARCHAR(2000), "
                + "created_at TIMESTAMP NOT NULL, "
                + "published_at TIMESTAMP)"
})
class OutboxServiceTest {

    @Autowired
    OutboxService outboxService;

    @Autowired
    OutboxEventRepository repository;

    @Test
    void enqueuePersistsPendingEvent() {
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        outboxService.enqueue("surplus.events", aggregateId.toString(), "{\"ok\":true}", eventId,
                "SurplusEvent", "Surplus", aggregateId);

        OutboxEvent saved = repository.findById(eventId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getTopic()).isEqualTo("surplus.events");
    }
}
