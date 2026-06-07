package com.police.eom.repo;

import com.police.eom.domain.FirearmAccountability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FirearmAccountabilityRepository extends JpaRepository<FirearmAccountability, Long> {
    Optional<FirearmAccountability> findByOfficerId(Long officerId);
}
