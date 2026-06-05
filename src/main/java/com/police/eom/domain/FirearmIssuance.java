package com.police.eom.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 枪械领用/归还记录。
 * status: ISSUED(已领用未归还) / RETURNED(已归还)
 */
@Entity
@Table(name = "firearm_issuances")
public class FirearmIssuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firearm_id", nullable = false)
    private Long firearmId;

    @Column(name = "officer_id", nullable = false)
    private Long officerId;

    @Column(nullable = false, length = 255)
    private String purpose = "";

    @Column(name = "ammo_issued", nullable = false)
    private int ammoIssued = 0;

    @Column(name = "ammo_returned")
    private Integer ammoReturned;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(nullable = false, length = 16)
    private String status = "ISSUED";

    @PrePersist
    public void prePersist() {
        if (issuedAt == null) issuedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFirearmId() { return firearmId; }
    public void setFirearmId(Long firearmId) { this.firearmId = firearmId; }
    public Long getOfficerId() { return officerId; }
    public void setOfficerId(Long officerId) { this.officerId = officerId; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public int getAmmoIssued() { return ammoIssued; }
    public void setAmmoIssued(int ammoIssued) { this.ammoIssued = ammoIssued; }
    public Integer getAmmoReturned() { return ammoReturned; }
    public void setAmmoReturned(Integer ammoReturned) { this.ammoReturned = ammoReturned; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
