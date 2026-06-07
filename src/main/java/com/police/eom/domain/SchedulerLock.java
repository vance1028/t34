package com.police.eom.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduler_locks")
public class SchedulerLock {

    @Id
    @Column(name = "lock_name", nullable = false, length = 64)
    private String lockName;

    @Column(name = "lock_holder", nullable = false, length = 128)
    private String lockHolder;

    @Column(name = "lock_until", nullable = false)
    private LocalDateTime lockUntil;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    public String getLockName() { return lockName; }
    public void setLockName(String lockName) { this.lockName = lockName; }
    public String getLockHolder() { return lockHolder; }
    public void setLockHolder(String lockHolder) { this.lockHolder = lockHolder; }
    public LocalDateTime getLockUntil() { return lockUntil; }
    public void setLockUntil(LocalDateTime lockUntil) { this.lockUntil = lockUntil; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }
}
