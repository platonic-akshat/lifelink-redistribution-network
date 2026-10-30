package com.lifelink.coordination.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organization")
public class Organization {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private OrganizationVerificationStatus verificationStatus;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Organization() {
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public OrganizationVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateVerification(OrganizationVerificationStatus status, Instant when) {
        this.verificationStatus = status;
        this.updatedAt = when;
    }
}
