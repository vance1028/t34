package com.police.eom.scheduler;

import com.police.eom.domain.FirearmIssuance;
import com.police.eom.domain.Officer;
import com.police.eom.repo.FirearmIssuanceRepository;
import com.police.eom.repo.OfficerRepository;
import com.police.eom.service.AccountabilityService;
import com.police.eom.service.SchedulerLockService;
import com.police.eom.service.SystemConfigService;
import com.police.eom.service.WarningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OverdueScannerScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueScannerScheduler.class);
    private static final String LOCK_NAME = "overdue_scanner";

    private final FirearmIssuanceRepository issuanceRepo;
    private final OfficerRepository officerRepo;
    private final WarningService warningService;
    private final AccountabilityService accountabilityService;
    private final SchedulerLockService lockService;
    private final SystemConfigService configService;

    public OverdueScannerScheduler(FirearmIssuanceRepository issuanceRepo,
                                   OfficerRepository officerRepo,
                                   WarningService warningService,
                                   AccountabilityService accountabilityService,
                                   SchedulerLockService lockService,
                                   SystemConfigService configService) {
        this.issuanceRepo = issuanceRepo;
        this.officerRepo = officerRepo;
        this.warningService = warningService;
        this.accountabilityService = accountabilityService;
        this.lockService = lockService;
        this.configService = configService;
    }

    @Scheduled(fixedDelayString = "${scheduler.scanner.interval:60000}")
    public void scanOverdueIssuances() {
        int ttlSeconds = configService.getLockTtlSeconds();

        if (!lockService.tryAcquireLock(LOCK_NAME, ttlSeconds)) {
            log.debug("Could not acquire scanner lock, another instance is running");
            return;
        }

        try {
            log.info("Starting overdue issuance scan");
            LocalDateTime startTime = LocalDateTime.now();

            accountabilityService.processExpiredRestrictions();

            List<FirearmIssuance> issued = issuanceRepo.findAllIssued();
            log.info("Found {} issued firearms to check", issued.size());

            int imminentCount = 0;
            int overdueCount = 0;
            int severeCount = 0;

            for (FirearmIssuance issuance : issued) {
                try {
                    Officer officer = officerRepo.findById(issuance.getOfficerId()).orElse(null);
                    if (officer == null) {
                        log.warn("Officer {} not found for issuance {}", issuance.getOfficerId(), issuance.getId());
                        continue;
                    }

                    WarningService.ProcessResult result = warningService.processIssuance(issuance, officer);
                    if (result.warning != null) {
                        switch (result.warning.getWarningLevel()) {
                            case com.police.eom.domain.FirearmWarning.LEVEL_IMMINENT -> imminentCount++;
                            case com.police.eom.domain.FirearmWarning.LEVEL_OVERDUE -> overdueCount++;
                            case com.police.eom.domain.FirearmWarning.LEVEL_SEVERE -> severeCount++;
                        }

                        if (result.newViolation && result.violationLevel != null) {
                            accountabilityService.recordViolation(
                                    issuance.getOfficerId(), result.violationLevel);
                        }
                    }

                    accountabilityService.updateUnreturnedStatus(issuance.getOfficerId());

                } catch (Exception e) {
                    log.error("Error processing issuance {}", issuance.getId(), e);
                }
            }

            Duration elapsed = Duration.between(startTime, LocalDateTime.now());
            log.info("Overdue scan completed in {}ms: imminent={}, overdue={}, severe={}",
                    elapsed.toMillis(), imminentCount, overdueCount, severeCount);

        } catch (Exception e) {
            log.error("Error during overdue scan", e);
        } finally {
            lockService.releaseLock(LOCK_NAME);
        }
    }
}
