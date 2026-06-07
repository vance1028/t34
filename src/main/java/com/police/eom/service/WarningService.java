package com.police.eom.service;

import com.police.eom.domain.FirearmIssuance;
import com.police.eom.domain.FirearmWarning;
import com.police.eom.domain.Officer;
import com.police.eom.repo.FirearmIssuanceRepository;
import com.police.eom.repo.FirearmWarningRepository;
import com.police.eom.repo.OfficerRepository;
import com.police.eom.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WarningService {

    private static final Logger log = LoggerFactory.getLogger(WarningService.class);

    private final FirearmWarningRepository warningRepo;
    private final FirearmIssuanceRepository issuanceRepo;
    private final OfficerRepository officerRepo;
    private final SystemConfigService configService;

    public WarningService(FirearmWarningRepository warningRepo,
                          FirearmIssuanceRepository issuanceRepo,
                          OfficerRepository officerRepo,
                          SystemConfigService configService) {
        this.warningRepo = warningRepo;
        this.issuanceRepo = issuanceRepo;
        this.officerRepo = officerRepo;
        this.configService = configService;
    }

    public List<FirearmWarning> list(String status, Long officerId) {
        if (status != null && !status.isBlank()) {
            if (officerId != null) {
                return warningRepo.findByOfficerIdAndStatus(officerId, status);
            }
            return warningRepo.findByStatus(status);
        }
        if (officerId != null) {
            return warningRepo.findByOfficerId(officerId);
        }
        return warningRepo.findAll();
    }

    public List<FirearmWarning> listActive() {
        return warningRepo.findAllActive();
    }

    public FirearmWarning get(Long id) {
        return warningRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("预警不存在"));
    }

    @Transactional
    public FirearmWarning processIssuance(FirearmIssuance issuance, Officer officer) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueAt = issuance.getDueAt();

        String currentLevel = calculateWarningLevel(now, dueAt);
        if (currentLevel == null) {
            return null;
        }

        Optional<FirearmWarning> existingOpt = warningRepo.findActiveByIssuanceId(issuance.getId());

        if (existingOpt.isPresent()) {
            FirearmWarning existing = existingOpt.get();
            if (!existing.getWarningLevel().equals(currentLevel)) {
                existing.setWarningLevel(currentLevel);
                existing.setEscalationCount(existing.getEscalationCount() + 1);
                existing.setLastNotifiedAt(now);
                FirearmWarning updated = warningRepo.save(existing);
                log.info("Escalated warning for issuance {} from {} to {}",
                        issuance.getId(), existing.getWarningLevel(), currentLevel);
                sendNotification(updated, officer);
                return updated;
            } else {
                Duration notifyInterval = configService.getNotificationInterval();
                if (existing.getLastNotifiedAt() == null ||
                        Duration.between(existing.getLastNotifiedAt(), now).compareTo(notifyInterval) >= 0) {
                    existing.setLastNotifiedAt(now);
                    FirearmWarning updated = warningRepo.save(existing);
                    sendNotification(updated, officer);
                    return updated;
                }
            }
            return existing;
        } else {
            FirearmWarning warning = new FirearmWarning();
            warning.setIssuanceId(issuance.getId());
            warning.setOfficerId(issuance.getOfficerId());
            warning.setDepartment(officer.getDepartment());
            warning.setWarningLevel(currentLevel);
            warning.setStatus(FirearmWarning.STATUS_OPEN);
            warning.setLastNotifiedAt(now);
            FirearmWarning saved = warningRepo.save(warning);
            log.info("Created {} warning for issuance {} (officer {})",
                    currentLevel, issuance.getId(), issuance.getOfficerId());
            sendNotification(saved, officer);
            return saved;
        }
    }

    private String calculateWarningLevel(LocalDateTime now, LocalDateTime dueAt) {
        Duration imminentThreshold = configService.getImminentThreshold();
        Duration severeThreshold = configService.getSevereThreshold();

        Duration untilDue = Duration.between(now, dueAt);

        if (untilDue.isNegative() || untilDue.isZero()) {
            Duration overdue = untilDue.abs();
            if (overdue.compareTo(severeThreshold) >= 0) {
                return FirearmWarning.LEVEL_SEVERE;
            }
            return FirearmWarning.LEVEL_OVERDUE;
        } else if (untilDue.compareTo(imminentThreshold) <= 0) {
            return FirearmWarning.LEVEL_IMMINENT;
        }
        return null;
    }

    private void sendNotification(FirearmWarning warning, Officer officer) {
        String level = warning.getWarningLevel();
        String department = officer.getDepartment();
        log.warn("[{} WARNING] Issuance #{}: Officer {} ({}/{}) due for return. Department: {}",
                level, warning.getIssuanceId(), officer.getName(), officer.getPoliceNo(),
                officer.getRankTitle(), department);

        if (FirearmWarning.LEVEL_SEVERE.equals(level)) {
            log.warn("*** MAJOR RISK ALERT *** Notify department head for {} - Officer {} has severely overdue firearm",
                    department, officer.getName());
        }
    }

    @Transactional
    public FirearmWarning confirm(Long warningId, Long confirmedBy) {
        FirearmWarning warning = get(warningId);
        if (FirearmWarning.STATUS_CLOSED.equals(warning.getStatus())) {
            throw ApiException.conflict("预警已关闭，无法确认");
        }
        if (!officerRepo.existsById(confirmedBy)) {
            throw ApiException.badRequest("确认人不存在");
        }
        warning.setStatus(FirearmWarning.STATUS_CONFIRMED);
        warning.setConfirmedBy(confirmedBy);
        warning.setConfirmedAt(LocalDateTime.now());
        return warningRepo.save(warning);
    }

    @Transactional
    public FirearmWarning transfer(Long warningId, Long transferredTo, Long transferredBy, String remark) {
        FirearmWarning warning = get(warningId);
        if (FirearmWarning.STATUS_CLOSED.equals(warning.getStatus())) {
            throw ApiException.conflict("预警已关闭，无法流转");
        }
        if (!officerRepo.existsById(transferredTo)) {
            throw ApiException.badRequest("接收人不存在");
        }
        if (!officerRepo.existsById(transferredBy)) {
            throw ApiException.badRequest("操作人不存在");
        }
        warning.setStatus(FirearmWarning.STATUS_TRANSFERRED);
        warning.setTransferredTo(transferredTo);
        warning.setTransferredBy(transferredBy);
        warning.setTransferredAt(LocalDateTime.now());
        return warningRepo.save(warning);
    }

    @Transactional
    public FirearmWarning close(Long warningId, Long closedBy, String reason) {
        FirearmWarning warning = get(warningId);
        if (FirearmWarning.STATUS_CLOSED.equals(warning.getStatus())) {
            throw ApiException.conflict("预警已关闭");
        }
        if (reason == null || reason.isBlank()) {
            throw ApiException.badRequest("关闭原因不能为空");
        }
        if (!officerRepo.existsById(closedBy)) {
            throw ApiException.badRequest("操作人不存在");
        }
        warning.setStatus(FirearmWarning.STATUS_CLOSED);
        warning.setClosedBy(closedBy);
        warning.setClosedAt(LocalDateTime.now());
        warning.setCloseReason(reason);
        return warningRepo.save(warning);
    }

    @Transactional
    public void closeWarningsForIssuance(Long issuanceId, Long closedBy, String reason) {
        Optional<FirearmWarning> warningOpt = warningRepo.findActiveByIssuanceId(issuanceId);
        if (warningOpt.isPresent()) {
            FirearmWarning w = warningOpt.get();
            w.setStatus(FirearmWarning.STATUS_CLOSED);
            w.setClosedBy(closedBy);
            w.setClosedAt(LocalDateTime.now());
            w.setCloseReason(reason != null ? reason : "枪支已归还");
            warningRepo.save(w);
            log.info("Closed warning {} for returned issuance {}", w.getId(), issuanceId);
        }
    }
}
