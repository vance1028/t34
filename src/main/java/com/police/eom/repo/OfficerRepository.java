package com.police.eom.repo;

import com.police.eom.domain.Officer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfficerRepository extends JpaRepository<Officer, Long> {
    boolean existsByPoliceNo(String policeNo);
    Optional<Officer> findByPoliceNo(String policeNo);
}
