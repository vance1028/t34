package com.police.eom.repo;

import com.police.eom.domain.FirearmIssuanceRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FirearmIssuanceRestrictionRepository extends JpaRepository<FirearmIssuanceRestriction, Long> {

    List<FirearmIssuanceRestriction> findByOfficerId(Long officerId);

    @Query("SELECT r FROM FirearmIssuanceRestriction r WHERE r.officerId = :officerId " +
           "AND r.status = 'ACTIVE' AND (r.expiresAt IS NULL OR r.expiresAt > :now)")
    Optional<FirearmIssuanceRestriction> findActiveByOfficerId(@Param("officerId") Long officerId,
                                                               @Param("now") LocalDateTime now);

    @Query("SELECT r FROM FirearmIssuanceRestriction r WHERE r.status = 'ACTIVE' AND r.expiresAt <= :now")
    List<FirearmIssuanceRestriction> findExpiredActiveRestrictions(@Param("now") LocalDateTime now);
}
