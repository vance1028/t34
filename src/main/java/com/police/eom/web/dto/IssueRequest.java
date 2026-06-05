package com.police.eom.web.dto;

import java.time.LocalDateTime;

/** 枪械领用请求体。 */
public class IssueRequest {
    private Long officerId;
    private String purpose;
    private int ammoIssued;
    private LocalDateTime dueAt;

    public Long getOfficerId() { return officerId; }
    public void setOfficerId(Long officerId) { this.officerId = officerId; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public int getAmmoIssued() { return ammoIssued; }
    public void setAmmoIssued(int ammoIssued) { this.ammoIssued = ammoIssued; }
    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
}
