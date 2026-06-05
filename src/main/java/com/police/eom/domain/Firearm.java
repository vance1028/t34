package com.police.eom.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 枪械。
 * status: IN_STORE(在库) / ISSUED(已领用) / MAINTENANCE(维修) / RETIRED(退役)
 */
@Entity
@Table(name = "firearms")
public class Firearm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_no", nullable = false, unique = true, length = 48)
    private String serialNo;

    @Column(nullable = false, length = 64)
    private String model;

    @Column(nullable = false, length = 32)
    private String type = "PISTOL";

    @Column(nullable = false, length = 32)
    private String caliber = "";

    @Column(nullable = false, length = 16)
    private String status = "IN_STORE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSerialNo() { return serialNo; }
    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCaliber() { return caliber; }
    public void setCaliber(String caliber) { this.caliber = caliber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
