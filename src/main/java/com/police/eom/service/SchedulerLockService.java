package com.police.eom.service;

import com.police.eom.domain.SchedulerLock;
import com.police.eom.repo.SchedulerLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SchedulerLockService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerLockService.class);

    private final SchedulerLockRepository lockRepo;
    private final String nodeId;

    public SchedulerLockService(SchedulerLockRepository lockRepo) {
        this.lockRepo = lockRepo;
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            host = "unknown";
        }
        this.nodeId = host + "-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Scheduler node ID: {}", nodeId);
    }

    public String getNodeId() {
        return nodeId;
    }

    @Transactional
    public boolean tryAcquireLock(String lockName, int ttlSeconds) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lockUntil = now.plusSeconds(ttlSeconds);

        try {
            lockRepo.deleteExpiredLocks(lockName, now);
        } catch (Exception e) {
            log.debug("Error cleaning expired locks for {}: {}", lockName, e.getMessage());
        }

        Optional<SchedulerLock> existing = lockRepo.findById(lockName);
        if (existing.isPresent()) {
            if (existing.get().getLockUntil().isAfter(now)) {
                if (nodeId.equals(existing.get().getLockHolder())) {
                    existing.get().setLockUntil(lockUntil);
                    lockRepo.save(existing.get());
                    return true;
                }
                return false;
            } else {
                lockRepo.delete(existing.get());
            }
        }

        SchedulerLock newLock = new SchedulerLock();
        newLock.setLockName(lockName);
        newLock.setLockHolder(nodeId);
        newLock.setLockUntil(lockUntil);
        newLock.setLockedAt(now);

        try {
            lockRepo.save(newLock);
            log.debug("Acquired lock: {} by {}", lockName, nodeId);
            return true;
        } catch (DataIntegrityViolationException e) {
            log.debug("Lock {} already acquired by another node", lockName);
            return false;
        }
    }

    public void releaseLock(String lockName) {
        try {
            lockRepo.releaseLock(lockName, nodeId);
            log.debug("Released lock: {} by {}", lockName, nodeId);
        } catch (Exception e) {
            log.debug("Error releasing lock {}: {}", lockName, e.getMessage());
        }
    }

    public boolean isLockHeld(String lockName) {
        Optional<SchedulerLock> lock = lockRepo.findActiveLock(lockName, LocalDateTime.now());
        return lock.isPresent() && nodeId.equals(lock.get().getLockHolder());
    }

    public boolean extendLock(String lockName, int ttlSeconds) {
        if (isLockHeld(lockName)) {
            try {
                Optional<SchedulerLock> lock = lockRepo.findById(lockName);
                if (lock.isPresent() && nodeId.equals(lock.get().getLockHolder())) {
                    lock.get().setLockUntil(LocalDateTime.now().plusSeconds(ttlSeconds));
                    lockRepo.save(lock.get());
                    return true;
                }
            } catch (Exception e) {
                log.debug("Error extending lock {}: {}", lockName, e.getMessage());
            }
        }
        return false;
    }
}
