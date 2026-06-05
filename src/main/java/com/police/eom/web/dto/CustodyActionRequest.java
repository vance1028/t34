package com.police.eom.web.dto;

/** 物证借出/归还请求体。 */
public class CustodyActionRequest {
    private Long officerId;
    private String remark;

    public Long getOfficerId() { return officerId; }
    public void setOfficerId(Long officerId) { this.officerId = officerId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
