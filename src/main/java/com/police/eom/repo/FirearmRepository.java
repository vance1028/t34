package com.police.eom.repo;

import com.police.eom.domain.Firearm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FirearmRepository extends JpaRepository<Firearm, Long> {
    boolean existsBySerialNo(String serialNo);
    Optional<Firearm> findBySerialNo(String serialNo);
    List<Firearm> findByStatus(String status);
}
