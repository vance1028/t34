package com.police.eom.repo;

import com.police.eom.domain.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
    boolean existsByEvidenceNo(String evidenceNo);
    Optional<Evidence> findByEvidenceNo(String evidenceNo);
    List<Evidence> findByCaseNo(String caseNo);
    List<Evidence> findByStatus(String status);
}
