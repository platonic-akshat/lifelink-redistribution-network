package com.lifelink.need.domain;

import com.lifelink.events.model.Category;
import com.lifelink.events.model.NeedStatus;
import com.lifelink.events.model.Urgency;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "need", indexes = {
        @Index(name = "idx_need_geohash5", columnList = "geohash5"),
        @Index(name = "idx_need_status", columnList = "status"),
        @Index(name = "idx_need_requester", columnList = "requester_id")
})
public class Need {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Urgency urgency;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false, length = 12)
    private String geohash5;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Column(name = "requester_name", nullable = false)
    private String requesterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NeedStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Need() {
        // JPA
    }

    public Need(UUID id, Category category, String subType, double quantity, String unit, Urgency urgency,
                double latitude, double longitude, String geohash5, UUID requesterId, String requesterName,
                NeedStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.category = category;
        this.subType = subType;
        this.quantity = quantity;
        this.unit = unit;
        this.urgency = urgency;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geohash5 = geohash5;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
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

    public Urgency getUrgency() {
        return urgency;
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

    public UUID getRequesterId() {
        return requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public NeedStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(NeedStatus status) {
        this.status = status;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
