package com.police.eom.repo;

import com.police.eom.domain.CustodyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustodyRecordRepository extends JpaRepository<CustodyRecord, Long> {
    List<CustodyRecord> findByEvidenceIdOrderByOccurredAtAscIdAsc(Long evidenceId);
}
