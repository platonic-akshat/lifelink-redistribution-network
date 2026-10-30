package com.lifelink.surplus.domain;

import com.lifelink.events.model.Category;
import com.lifelink.events.model.SurplusStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "surplus", indexes = {
        @Index(name = "idx_surplus_geohash5", columnList = "geohash5"),
        @Index(name = "idx_surplus_status", columnList = "status"),
        @Index(name = "idx_surplus_provider", columnList = "provider_id")
})
public class Surplus {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(name = "sub_type", nullable = false)
    private String subType;

    @Column(nullable = false)
    private double quantity;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    /**
     * 5-char geohash bucket (~4.9km cell). V1 uses this for nearby lookups
     * instead of a PostGIS GEOGRAPHY column — see ARCHITECTURE.md §6 for the
     * planned upgrade to GIST-indexed radius queries.
     */
    @Column(nullable = false, length = 12)
    private String geohash5;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Column(name = "provider_name", nullable = false)
    private String providerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SurplusStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Surplus() {
        // JPA
    }

    public Surplus(UUID id, Category category, String subType, double quantity, String unit,
                   double latitude, double longitude, String geohash5, Instant expiresAt,
                   UUID providerId, String providerName, SurplusStatus status,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.category = category;
        this.subType = subType;
        this.quantity = quantity;
        this.unit = unit;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geohash5 = geohash5;
        this.expiresAt = expiresAt;
        this.providerId = providerId;
        this.providerName = providerName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public String getSubType() {
        return subType;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getGeohash5() {
        return geohash5;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public SurplusStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(SurplusStatus status) {
        this.status = status;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
