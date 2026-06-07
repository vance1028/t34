package com.police.eom.web.dto;

public class WarningActionRequest {
    private Long operatorId;
    private String reason;
    private Long targetOfficerId;

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getTargetOfficerId() { return targetOfficerId; }
    public void setTargetOfficerId(Long targetOfficerId) { this.targetOfficerId = targetOfficerId; }
}
