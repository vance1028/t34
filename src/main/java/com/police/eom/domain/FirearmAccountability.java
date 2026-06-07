package com.police.eom.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "firearm_accountability")
public class FirearmAccountability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "officer_id", nullable = false, unique = true)
    private Long officerId;

    @Column(name = "overdue_count", nullable = false)
    private int overdueCount = 0;

    @Column(name = "severe_overdue_count", nullable = false)
    private int severeOverdueCount = 0;

    @Column(name = "total_violations", nullable = false)
    private int totalViolations = 0;

    @Column(name = "has_unreturned", nullable = false)
    private boolean hasUnreturned = false;

    @Column(name = "last_violation_at")
    private LocalDateTime lastViolationAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOfficerId() { return officerId; }
    public void setOfficerId(Long officerId) { this.officerId = officerId; }
    public int getOverdueCount() { return overdueCount; }
    public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }
    public int getSevereOverdueCount() { return severeOverdueCount; }
    public void setSevereOverdueCount(int severeOverdueCount) { this.severeOverdueCount = severeOverdueCount; }
    public int getTotalViolations() { return totalViolations; }
    public void setTotalViolations(int totalViolations) { this.totalViolations = totalViolations; }
    public boolean isHasUnreturned() { return hasUnreturned; }
    public void setHasUnreturned(boolean hasUnreturned) { this.hasUnreturned = hasUnreturned; }
    public LocalDateTime getLastViolationAt() { return lastViolationAt; }
    public void setLastViolationAt(LocalDateTime lastViolationAt) { this.lastViolationAt = lastViolationAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
