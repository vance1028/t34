package com.police.eom.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 物证。
 * status 流转：REGISTERED(已登记) → IN_STORAGE(在库) → CHECKED_OUT(借出) → IN_STORAGE → DESTROYED/RELEASED(销毁/发还)
 */
@Entity
@Table(name = "evidence")
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evidence_no", nullable = false, unique = true, length = 48)
    private String evidenceNo;

    @Column(name = "case_no", nullable = false, length = 48)
    private String caseNo;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 32)
    private String category = "OTHER";

    @Column(nullable = false, length = 1000)
    private String description = "";

    @Column(nullable = false, length = 16)
    private String status = "REGISTERED";

    @Column(nullable = false, length = 128)
    private String location = "";

    @Column(name = "registered_by")
    private Long registeredBy;

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
    public String getEvidenceNo() { return evidenceNo; }
    public void setEvidenceNo(String evidenceNo) { this.evidenceNo = evidenceNo; }
    public String getCaseNo() { return caseNo; }
    public void setCaseNo(String caseNo) { this.caseNo = caseNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Long getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(Long registeredBy) { this.registeredBy = registeredBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
