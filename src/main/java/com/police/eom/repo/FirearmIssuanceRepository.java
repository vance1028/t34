package com.police.eom.repo;

import com.police.eom.domain.FirearmIssuance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FirearmIssuanceRepository extends JpaRepository<FirearmIssuance, Long> {
    List<FirearmIssuance> findByOfficerId(Long officerId);
    List<FirearmIssuance> findByFirearmId(Long firearmId);
    List<FirearmIssuance> findByStatus(String status);
    Optional<FirearmIssuance> findFirstByFirearmIdAndStatusOrderByIssuedAtDesc(Long firearmId, String status);
}
