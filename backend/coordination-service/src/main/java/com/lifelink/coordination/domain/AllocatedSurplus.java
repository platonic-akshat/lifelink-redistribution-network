package com.lifelink.coordination.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "allocated_surplus")
public class AllocatedSurplus {

    @Id
    @Column(name = "surplus_id")
    private UUID surplusId;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    protected AllocatedSurplus() {
    }

    public AllocatedSurplus(UUID surplusId, UUID matchId) {
        this.surplusId = surplusId;
        this.matchId = matchId;
    }
}
