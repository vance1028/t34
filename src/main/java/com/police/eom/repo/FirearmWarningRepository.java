package com.police.eom.repo;

import com.police.eom.domain.FirearmWarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FirearmWarningRepository extends JpaRepository<FirearmWarning, Long> {

    List<FirearmWarning> findByStatus(String status);

    List<FirearmWarning> findByOfficerId(Long officerId);

    List<FirearmWarning> findByWarningLevel(String warningLevel);

    @Query("SELECT w FROM FirearmWarning w WHERE w.issuanceId = :issuanceId AND w.status != 'CLOSED'")
    Optional<FirearmWarning> findActiveByIssuanceId(@Param("issuanceId") Long issuanceId);

    @Query("SELECT w FROM FirearmWarning w WHERE w.status != 'CLOSED'")
    List<FirearmWarning> findAllActive();

    List<FirearmWarning> findByOfficerIdAndStatus(Long officerId, String status);
}
