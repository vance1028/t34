package com.police.eom.service;

import com.police.eom.domain.Firearm;
import com.police.eom.domain.FirearmIssuance;
import com.police.eom.repo.FirearmIssuanceRepository;
import com.police.eom.repo.FirearmRepository;
import com.police.eom.repo.OfficerRepository;
import com.police.eom.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FirearmService {

    private final FirearmRepository firearmRepo;
    private final FirearmIssuanceRepository issuanceRepo;
    private final OfficerRepository officerRepo;
    private final AccountabilityService accountabilityService;
    private final WarningService warningService;

    public FirearmService(FirearmRepository firearmRepo,
                          FirearmIssuanceRepository issuanceRepo,
                          OfficerRepository officerRepo,
                          AccountabilityService accountabilityService,
                          WarningService warningService) {
        this.firearmRepo = firearmRepo;
        this.issuanceRepo = issuanceRepo;
        this.officerRepo = officerRepo;
        this.accountabilityService = accountabilityService;
        this.warningService = warningService;
    }

    public List<Firearm> list(String status) {
        if (status != null && !status.isBlank()) return firearmRepo.findByStatus(status);
        return firearmRepo.findAll();
    }

    public Firearm get(Long id) {
        return firearmRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("枪械不存在"));
    }

    @Transactional
    public Firearm register(Firearm input) {
        if (input.getSerialNo() == null || input.getSerialNo().isBlank()) {
            throw ApiException.badRequest("枪械编号不能为空");
        }
        if (input.getModel() == null || input.getModel().isBlank()) {
            throw ApiException.badRequest("枪械型号不能为空");
        }
        if (firearmRepo.existsBySerialNo(input.getSerialNo())) {
            throw ApiException.conflict("枪械编号已存在");
        }
        input.setId(null);
        input.setStatus("IN_STORE");
        return firearmRepo.save(input);
    }

    @Transactional
    public FirearmIssuance issue(Long firearmId, Long officerId, String purpose,
                                 int ammoIssued, LocalDateTime dueAt) {
        Firearm firearm = get(firearmId);
        if (!"IN_STORE".equals(firearm.getStatus())) {
            throw ApiException.conflict("枪械当前不可领用，状态：" + firearm.getStatus());
        }
        if (officerId == null || !officerRepo.existsById(officerId)) {
            throw ApiException.badRequest("领用民警不存在");
        }
        if (ammoIssued < 0) {
            throw ApiException.badRequest("发放弹药数不能为负");
        }
        if (dueAt == null || !dueAt.isAfter(LocalDateTime.now())) {
            throw ApiException.badRequest("应归还时间必须晚于当前时间");
        }

        if (accountabilityService.isOfficerRestricted(officerId)) {
            var restriction = accountabilityService.getActiveRestriction(officerId);
            String msg = restriction.map(r -> "该民警处于领枪限制状态，原因：" + r.getReason() +
                    "，限制到期：" + r.getExpiresAt()).orElse("该民警处于领枪限制状态");
            throw ApiException.forbidden(msg);
        }

        firearm.setStatus("ISSUED");
        firearmRepo.save(firearm);

        FirearmIssuance iss = new FirearmIssuance();
        iss.setFirearmId(firearmId);
        iss.setOfficerId(officerId);
        iss.setPurpose(purpose == null ? "" : purpose);
        iss.setAmmoIssued(ammoIssued);
        iss.setDueAt(dueAt);
        iss.setStatus("ISSUED");
        FirearmIssuance saved = issuanceRepo.save(iss);

        accountabilityService.updateUnreturnedStatus(officerId);

        return saved;
    }

    @Transactional
    public FirearmIssuance returnFirearm(Long firearmId, Integer ammoReturned) {
        Firearm firearm = get(firearmId);
        if (!"ISSUED".equals(firearm.getStatus())) {
            throw ApiException.conflict("枪械未处于领用状态，无法归还，状态：" + firearm.getStatus());
        }
        FirearmIssuance iss = issuanceRepo
                .findFirstByFirearmIdAndStatusOrderByIssuedAtDesc(firearmId, "ISSUED")
                .orElseThrow(() -> ApiException.conflict("找不到该枪械的未归还领用记录"));

        if (ammoReturned != null) {
            if (ammoReturned < 0) throw ApiException.badRequest("归还弹药数不能为负");
            if (ammoReturned > iss.getAmmoIssued()) {
                throw ApiException.badRequest("归还弹药数不能超过发放数");
            }
            iss.setAmmoReturned(ammoReturned);
        }
        iss.setStatus("RETURNED");
        iss.setReturnedAt(LocalDateTime.now());
        issuanceRepo.save(iss);

        firearm.setStatus("IN_STORE");
        firearmRepo.save(firearm);

        warningService.closeWarningsForIssuance(iss.getId(), null, null);
        accountabilityService.updateUnreturnedStatus(iss.getOfficerId());

        return iss;
    }

    public List<FirearmIssuance> issuancesByFirearm(Long firearmId) {
        get(firearmId);
        return issuanceRepo.findByFirearmId(firearmId);
    }
}
