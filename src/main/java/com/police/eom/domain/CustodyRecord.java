package com.police.eom.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 物证保管链记录（chain of custody）。
 * action: REGISTER(登记) / CHECK_OUT(借出) / CHECK_IN(归还) / TRANSFER(移交) / DISPOSE(处置)
 */
@Entity
@Table(name = "custody_records")
public class CustodyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evidence_id", nullable = false)
    private Long evidenceId;

    @Column(nullable = false, length = 16)
    private String action;

    @Column(name = "from_officer")
    private Long fromOfficer;

    @Column(name = "to_officer")
    private Long toOfficer;

    @Column(nullable = false, length = 500)
    private String remark = "";

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    public void prePersist() {
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEvidenceId() { return evidenceId; }
    public void setEvidenceId(Long evidenceId) { this.evidenceId = evidenceId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getFromOfficer() { return fromOfficer; }
    public void setFromOfficer(Long fromOfficer) { this.fromOfficer = fromOfficer; }
    public Long getToOfficer() { return toOfficer; }
    public void setToOfficer(Long toOfficer) { this.toOfficer = toOfficer; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
