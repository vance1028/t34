package com.police.eom.service;

import com.police.eom.domain.CustodyRecord;
import com.police.eom.domain.Evidence;
import com.police.eom.repo.CustodyRecordRepository;
import com.police.eom.repo.EvidenceRepository;
import com.police.eom.repo.OfficerRepository;
import com.police.eom.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 物证业务：登记、借出/归还、保管链留痕。 */
@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepo;
    private final CustodyRecordRepository custodyRepo;
    private final OfficerRepository officerRepo;

    public EvidenceService(EvidenceRepository evidenceRepo,
                           CustodyRecordRepository custodyRepo,
                           OfficerRepository officerRepo) {
        this.evidenceRepo = evidenceRepo;
        this.custodyRepo = custodyRepo;
        this.officerRepo = officerRepo;
    }

    public List<Evidence> list(String caseNo, String status) {
        if (caseNo != null && !caseNo.isBlank()) return evidenceRepo.findByCaseNo(caseNo);
        if (status != null && !status.isBlank()) return evidenceRepo.findByStatus(status);
        return evidenceRepo.findAll();
    }

    public Evidence get(Long id) {
        return evidenceRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("物证不存在"));
    }

    @Transactional
    public Evidence register(Evidence input) {
        if (input.getEvidenceNo() == null || input.getEvidenceNo().isBlank()) {
            throw ApiException.badRequest("物证编号不能为空");
        }
        if (input.getCaseNo() == null || input.getCaseNo().isBlank()) {
            throw ApiException.badRequest("案件编号不能为空");
        }
        if (input.getName() == null || input.getName().isBlank()) {
            throw ApiException.badRequest("物证名称不能为空");
        }
        if (evidenceRepo.existsByEvidenceNo(input.getEvidenceNo())) {
            throw ApiException.conflict("物证编号已存在");
        }
        if (input.getRegisteredBy() != null && !officerRepo.existsById(input.getRegisteredBy())) {
            throw ApiException.badRequest("登记民警不存在");
        }
        input.setId(null);
        input.setStatus("IN_STORAGE");
        Evidence saved = evidenceRepo.save(input);

        CustodyRecord rec = new CustodyRecord();
        rec.setEvidenceId(saved.getId());
        rec.setAction("REGISTER");
        rec.setToOfficer(saved.getRegisteredBy());
        rec.setRemark("入库登记");
        custodyRepo.save(rec);

        return saved;
    }

    @Transactional
    public Evidence checkOut(Long evidenceId, Long toOfficer, String remark) {
        Evidence ev = get(evidenceId);
        if (!"IN_STORAGE".equals(ev.getStatus())) {
            throw ApiException.conflict("物证当前状态不可借出：" + ev.getStatus());
        }
        if (toOfficer == null || !officerRepo.existsById(toOfficer)) {
            throw ApiException.badRequest("借出对象民警不存在");
        }
        Long from = ev.getRegisteredBy();
        ev.setStatus("CHECKED_OUT");
        evidenceRepo.save(ev);

        CustodyRecord rec = new CustodyRecord();
        rec.setEvidenceId(evidenceId);
        rec.setAction("CHECK_OUT");
        rec.setFromOfficer(from);
        rec.setToOfficer(toOfficer);
        rec.setRemark(remark == null ? "" : remark);
        custodyRepo.save(rec);
        return ev;
    }

    @Transactional
    public Evidence checkIn(Long evidenceId, Long byOfficer, String remark) {
        Evidence ev = get(evidenceId);
        if (!"CHECKED_OUT".equals(ev.getStatus())) {
            throw ApiException.conflict("物证未处于借出状态，无法归还：" + ev.getStatus());
        }
        ev.setStatus("IN_STORAGE");
        evidenceRepo.save(ev);

        CustodyRecord rec = new CustodyRecord();
        rec.setEvidenceId(evidenceId);
        rec.setAction("CHECK_IN");
        rec.setToOfficer(byOfficer);
        rec.setRemark(remark == null ? "" : remark);
        custodyRepo.save(rec);
        return ev;
    }

    public List<CustodyRecord> custodyChain(Long evidenceId) {
        get(evidenceId);
        return custodyRepo.findByEvidenceIdOrderByOccurredAtAscIdAsc(evidenceId);
    }
}
