package com.police.eom.service;

import com.police.eom.domain.FirearmAccountability;
import com.police.eom.domain.FirearmIssuanceRestriction;
import com.police.eom.domain.FirearmWarning;
import com.police.eom.repo.FirearmAccountabilityRepository;
import com.police.eom.repo.FirearmIssuanceRepository;
import com.police.eom.repo.FirearmIssuanceRestrictionRepository;
import com.police.eom.repo.OfficerRepository;
import com.police.eom.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccountabilityService {

    private static final Logger log = LoggerFactory.getLogger(AccountabilityService.class);

    private final FirearmAccountabilityRepository accountabilityRepo;
    private final FirearmIssuanceRestrictionRepository restrictionRepo;
    private final FirearmIssuanceRepository issuanceRepo;
    private final OfficerRepository officerRepo;
    private final SystemConfigService configService;

    public AccountabilityService(FirearmAccountabilityRepository accountabilityRepo,
                                 FirearmIssuanceRestrictionRepository restrictionRepo,
                                 FirearmIssuanceRepository issuanceRepo,
                                 OfficerRepository officerRepo,
                                 SystemConfigService configService) {
        this.accountabilityRepo = accountabilityRepo;
        this.restrictionRepo = restrictionRepo;
        this.issuanceRepo = issuanceRepo;
        this.officerRepo = officerRepo;
        this.configService = configService;
    }

    public FirearmAccountability getAccountability(Long officerId) {
        return accountabilityRepo.findByOfficerId(officerId)
                .orElseGet(() -> {
                    FirearmAccountability a = new FirearmAccountability();
                    a.setOfficerId(officerId);
                    return a;
                });
    }

    public List<FirearmAccountability> listAllAccountability() {
        return accountabilityRepo.findAll();
    }

    @Transactional
    public void recordViolation(Long officerId, String warningLevel) {
        FirearmAccountability acc = accountabilityRepo.findByOfficerId(officerId)
                .orElseGet(() -> {
                    FirearmAccountability a = new FirearmAccountability();
                    a.setOfficerId(officerId);
                    return a;
                });

        boolean isSevere = FirearmWarning.LEVEL_SEVERE.equals(warningLevel);
        if (isSevere) {
            acc.setSevereOverdueCount(acc.getSevereOverdueCount() + 1);
        } else if (FirearmWarning.LEVEL_OVERDUE.equals(warningLevel)) {
            acc.setOverdueCount(acc.getOverdueCount() + 1);
        }

        acc.setTotalViolations(acc.getOverdueCount() + acc.getSevereOverdueCount());
        acc.setHasUnreturned(issuanceRepo.hasUnreturnedByOfficerId(officerId));
        acc.setLastViolationAt(LocalDateTime.now());

        accountabilityRepo.save(acc);
        log.info("Recorded violation for officer {}: level={}, total={}",
                officerId, warningLevel, acc.getTotalViolations());

        checkAndApplyRestriction(officerId, acc);
    }

    @Transactional
    public void updateUnreturnedStatus(Long officerId) {
        FirearmAccountability acc = accountabilityRepo.findByOfficerId(officerId)
                .orElseGet(() -> {
                    FirearmAccountability a = new FirearmAccountability();
                    a.setOfficerId(officerId);
                    return a;
                });
        boolean hasUnreturned = issuanceRepo.hasUnreturnedByOfficerId(officerId);
        acc.setHasUnreturned(hasUnreturned);
        accountabilityRepo.save(acc);
    }

    private void checkAndApplyRestriction(Long officerId, FirearmAccountability acc) {
        int threshold = configService.getRestrictionThreshold();
        if (acc.getTotalViolations() >= threshold) {
            Optional<FirearmIssuanceRestriction> existing =
                    restrictionRepo.findActiveByOfficerId(officerId, LocalDateTime.now());
            if (existing.isEmpty()) {
                createRestriction(officerId,
                        FirearmIssuanceRestriction.TYPE_VIOLATION_ACCUMULATED,
                        "累计违规" + acc.getTotalViolations() + "次，达到限制阈值",
                        null);
            }
        }
    }

    @Transactional
    public FirearmIssuanceRestriction createRestriction(Long officerId, String type,
                                                        String reason, Long restrictedBy) {
        if (!officerRepo.existsById(officerId)) {
            throw ApiException.badRequest("民警不存在");
        }
        if (restrictedBy != null && !officerRepo.existsById(restrictedBy)) {
            throw ApiException.badRequest("限制操作人不存在");
        }

        FirearmIssuanceRestriction r = new FirearmIssuanceRestriction();
        r.setOfficerId(officerId);
        r.setRestrictionType(type);
        r.setReason(reason != null ? reason : "");
        r.setRestrictedBy(restrictedBy);
        r.setStatus(FirearmIssuanceRestriction.STATUS_ACTIVE);

        int defaultDays = configService.getDefaultRestrictionDays();
        r.setExpiresAt(LocalDateTime.now().plusDays(defaultDays));

        FirearmIssuanceRestriction saved = restrictionRepo.save(r);
        log.warn("Created issuance restriction for officer {}: type={}, expires={}",
                officerId, type, saved.getExpiresAt());
        return saved;
    }

    @Transactional
    public FirearmIssuanceRestriction liftRestriction(Long restrictionId, Long liftedBy, String reason) {
        FirearmIssuanceRestriction r = restrictionRepo.findById(restrictionId)
                .orElseThrow(() -> ApiException.notFound("限制记录不存在"));
        if (!FirearmIssuanceRestriction.STATUS_ACTIVE.equals(r.getStatus())) {
            throw ApiException.conflict("该限制当前不处于激活状态");
        }
        if (liftedBy != null && !officerRepo.existsById(liftedBy)) {
            throw ApiException.badRequest("解除操作人不存在");
        }
        r.setStatus(FirearmIssuanceRestriction.STATUS_LIFTED);
        r.setLiftedBy(liftedBy);
        r.setLiftedAt(LocalDateTime.now());
        r.setLiftReason(reason != null ? reason : "");
        log.info("Lifted restriction {} for officer {} by {}", restrictionId, r.getOfficerId(), liftedBy);
        return restrictionRepo.save(r);
    }

    public boolean isOfficerRestricted(Long officerId) {
        return restrictionRepo.findActiveByOfficerId(officerId, LocalDateTime.now()).isPresent();
    }

    public Optional<FirearmIssuanceRestriction> getActiveRestriction(Long officerId) {
        return restrictionRepo.findActiveByOfficerId(officerId, LocalDateTime.now());
    }

    public List<FirearmIssuanceRestriction> getRestrictionHistory(Long officerId) {
        return restrictionRepo.findByOfficerId(officerId);
    }

    @Transactional
    public int processExpiredRestrictions() {
        LocalDateTime now = LocalDateTime.now();
        List<FirearmIssuanceRestriction> expired = restrictionRepo.findExpiredActiveRestrictions(now);
        int count = 0;
        for (FirearmIssuanceRestriction r : expired) {
            r.setStatus(FirearmIssuanceRestriction.STATUS_EXPIRED);
            r.setLiftReason("限制期限到期自动解除");
            restrictionRepo.save(r);
            count++;
            log.info("Auto-expired restriction {} for officer {}", r.getId(), r.getOfficerId());
        }
        if (count > 0) {
            log.info("Processed {} expired restrictions", count);
        }
        return count;
    }

    public List<FirearmIssuanceRestriction> listAllRestrictions(String status) {
        if (status != null && !status.isBlank()) {
            return restrictionRepo.findAll().stream()
                    .filter(r -> status.equals(r.getStatus()))
                    .toList();
        }
        return restrictionRepo.findAll();
    }
}
