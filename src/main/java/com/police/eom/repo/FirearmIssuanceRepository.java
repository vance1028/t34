package com.police.eom.repo;

import com.police.eom.domain.FirearmIssuance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FirearmIssuanceRepository extends JpaRepository<FirearmIssuance, Long> {
    List<FirearmIssuance> findByOfficerId(Long officerId);
    List<FirearmIssuance> findByFirearmId(Long firearmId);
    List<FirearmIssuance> findByStatus(String status);
    Optional<FirearmIssuance> findFirstByFirearmIdAndStatusOrderByIssuedAtDesc(Long firearmId, String status);

    @Query("SELECT i FROM FirearmIssuance i WHERE i.status = 'ISSUED'")
    List<FirearmIssuance> findAllIssued();

    @Query("SELECT i FROM FirearmIssuance i WHERE i.status = 'ISSUED' AND i.officerId = :officerId")
    List<FirearmIssuance> findIssuedByOfficerId(@Param("officerId") Long officerId);

    @Query("SELECT COUNT(i) > 0 FROM FirearmIssuance i WHERE i.officerId = :officerId AND i.status = 'ISSUED'")
    boolean hasUnreturnedByOfficerId(@Param("officerId") Long officerId);
}
