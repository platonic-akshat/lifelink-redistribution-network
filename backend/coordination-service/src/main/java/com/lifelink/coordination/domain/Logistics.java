package com.lifelink.coordination.domain;

import com.lifelink.events.model.LogisticsStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "logistics", indexes = {
        @Index(name = "idx_logistics_status", columnList = "status")
})
public class Logistics {

    @Id
    @Column(name = "match_id")
    private UUID matchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LogisticsStatus status;

    @Column(length = 1000)
    private String notes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Logistics() {
        // JPA
    }

    public Logistics(UUID matchId, LogisticsStatus status, String notes, Instant updatedAt) {
        this.matchId = matchId;
        this.status = status;
        this.notes = notes;
        this.updatedAt = updatedAt;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public LogisticsStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(LogisticsStatus status, String notes, Instant updatedAt) {
        this.status = status;
        if (notes != null) {
            this.notes = notes;
        }
        this.updatedAt = updatedAt;
    }
}
