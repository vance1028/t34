package com.police.eom.repo;

import com.police.eom.domain.SchedulerLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SchedulerLockRepository extends JpaRepository<SchedulerLock, String> {

    @Query("SELECT l FROM SchedulerLock l WHERE l.lockName = :lockName AND l.lockUntil > :now")
    Optional<SchedulerLock> findActiveLock(@Param("lockName") String lockName,
                                           @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM SchedulerLock l WHERE l.lockName = :lockName AND l.lockUntil <= :now")
    int deleteExpiredLocks(@Param("lockName") String lockName,
                           @Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM SchedulerLock l WHERE l.lockName = :lockName AND l.lockHolder = :lockHolder")
    void releaseLock(@Param("lockName") String lockName,
                     @Param("lockHolder") String lockHolder);
}
