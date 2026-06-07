package com.police.eom.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "firearm_issuance_restrictions")
public class FirearmIssuanceRestriction {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_LIFTED = "LIFTED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    public static final String TYPE_VIOLATION_ACCUMULATED = "VIOLATION_ACCUMULATED";
    public static final String TYPE_ADMINISTRATIVE = "ADMINISTRATIVE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "officer_id", nullable = false)
    private Long officerId;

    @Column(name = "restriction_type", nullable = false, length = 32)
    private String restrictionType;

    @Column(nullable = false, length = 500)
    private String reason = "";

    @Column(name = "restricted_by")
    private Long restrictedBy;

    @Column(name = "restricted_at")
    private LocalDateTime restrictedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(nullable = false, length = 16)
    private String status = STATUS_ACTIVE;

    @Column(name = "lifted_by")
    private Long liftedBy;

    @Column(name = "lifted_at")
    private LocalDateTime liftedAt;

    @Column(name = "lift_reason", nullable = false, length = 500)
    private String liftReason = "";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (restrictedAt == null) restrictedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOfficerId() { return officerId; }
    public void setOfficerId(Long officerId) { this.officerId = officerId; }
    public String getRestrictionType() { return restrictionType; }
    public void setRestrictionType(String restrictionType) { this.restrictionType = restrictionType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getRestrictedBy() { return restrictedBy; }
    public void setRestrictedBy(Long restrictedBy) { this.restrictedBy = restrictedBy; }
    public LocalDateTime getRestrictedAt() { return restrictedAt; }
    public void setRestrictedAt(LocalDateTime restrictedAt) { this.restrictedAt = restrictedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getLiftedBy() { return liftedBy; }
    public void setLiftedBy(Long liftedBy) { this.liftedBy = liftedBy; }
    public LocalDateTime getLiftedAt() { return liftedAt; }
    public void setLiftedAt(LocalDateTime liftedAt) { this.liftedAt = liftedAt; }
    public String getLiftReason() { return liftReason; }
    public void setLiftReason(String liftReason) { this.liftReason = liftReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
