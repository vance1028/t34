package com.police.eom.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 民警。 */
@Entity
@Table(name = "officers")
public class Officer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "police_no", nullable = false, unique = true, length = 32)
    private String policeNo;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 128)
    private String department = "";

    @Column(name = "rank_title", nullable = false, length = 64)
    private String rankTitle = "";

    @Column(nullable = false, length = 16)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPoliceNo() { return policeNo; }
    public void setPoliceNo(String policeNo) { this.policeNo = policeNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRankTitle() { return rankTitle; }
    public void setRankTitle(String rankTitle) { this.rankTitle = rankTitle; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
