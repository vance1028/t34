package com.police.eom;

import com.police.eom.domain.FirearmWarning;
import com.police.eom.repo.FirearmAccountabilityRepository;
import com.police.eom.repo.FirearmIssuanceRepository;
import com.police.eom.repo.FirearmWarningRepository;
import com.police.eom.repo.OfficerRepository;
import com.police.eom.service.AccountabilityService;
import com.police.eom.service.WarningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WarningAndAccountabilityTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    FirearmWarningRepository warningRepo;

    @Autowired
    FirearmAccountabilityRepository accountabilityRepo;

    @Autowired
    FirearmIssuanceRepository issuanceRepo;

    @Autowired
    OfficerRepository officerRepo;

    @Autowired
    WarningService warningService;

    @Autowired
    AccountabilityService accountabilityService;

    @BeforeEach
    void cleanup() {
        warningRepo.deleteAll();
    }

    @Test
    void test_closeWarning_thenGenerateNew_thenCloseAgain_shouldSucceed() throws Exception {
        var issuance = issuanceRepo.findById(2L).orElseThrow();
        var officer = officerRepo.findById(1L).orElseThrow();

        var result1 = warningService.processIssuance(issuance, officer);
        assertNotNull(result1.warning);
        assertEquals(FirearmWarning.LEVEL_SEVERE, result1.warning.getWarningLevel());
        Long warningId1 = result1.warning.getId();

        mvc.perform(post("/api/warnings/" + warningId1 + "/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operatorId\":1,\"reason\":\"已通知民警\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        warningRepo.flush();

        var result2 = warningService.processIssuance(issuance, officer);
        assertNotNull(result2.warning);
        assertNotEquals(warningId1, result2.warning.getId());
        assertEquals(FirearmWarning.LEVEL_SEVERE, result2.warning.getWarningLevel());
        Long warningId2 = result2.warning.getId();

        mvc.perform(post("/api/warnings/" + warningId2 + "/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operatorId\":1,\"reason\":\"再次确认关闭\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        var w1 = warningRepo.findById(warningId1).orElseThrow();
        var w2 = warningRepo.findById(warningId2).orElseThrow();
        assertEquals(FirearmWarning.STATUS_CLOSED, w1.getStatus());
        assertEquals(FirearmWarning.STATUS_CLOSED, w2.getStatus());
    }

    @Test
    void test_sameIssuance_multipleProcesses_shouldNotDuplicateViolation() throws Exception {
        var issuance = issuanceRepo.findById(3L).orElseThrow();
        var officer = officerRepo.findById(2L).orElseThrow();

        var accBefore = accountabilityService.getAccountability(2L);
        int beforeTotal = accBefore.getTotalViolations();
        int beforeSevere = accBefore.getSevereOverdueCount();

        var result1 = warningService.processIssuance(issuance, officer);
        if (result1.newViolation && result1.violationLevel != null) {
            accountabilityService.recordViolation(2L, result1.violationLevel);
        }

        var result2 = warningService.processIssuance(issuance, officer);
        if (result2.newViolation && result2.violationLevel != null) {
            accountabilityService.recordViolation(2L, result2.violationLevel);
        }

        var result3 = warningService.processIssuance(issuance, officer);
        if (result3.newViolation && result3.violationLevel != null) {
            accountabilityService.recordViolation(2L, result3.violationLevel);
        }

        var accAfter = accountabilityService.getAccountability(2L);

        assertTrue(accAfter.getTotalViolations() - beforeTotal <= 1,
                "多次扫描不应重复记录违规，期望最多+1，实际+" + (accAfter.getTotalViolations() - beforeTotal));
    }

    @Test
    void test_warningClosed_newWarning_sameLevel_shouldNotDuplicateViolation() throws Exception {
        var issuance = issuanceRepo.findById(1L).orElseThrow();
        var officer = officerRepo.findById(4L).orElseThrow();

        var accBefore = accountabilityService.getAccountability(4L);
        int beforeTotal = accBefore.getTotalViolations();

        var result1 = warningService.processIssuance(issuance, officer);
        assertNotNull(result1.warning);
        if (result1.newViolation && result1.violationLevel != null) {
            accountabilityService.recordViolation(4L, result1.violationLevel);
        }

        warningService.close(result1.warning.getId(), 1L, "测试关闭");

        warningRepo.flush();

        var result2 = warningService.processIssuance(issuance, officer);
        assertNotNull(result2.warning);
        if (result2.newViolation && result2.violationLevel != null) {
            accountabilityService.recordViolation(4L, result2.violationLevel);
        }

        var result3 = warningService.processIssuance(issuance, officer);
        assertNotNull(result3.warning);
        if (result3.newViolation && result3.violationLevel != null) {
            accountabilityService.recordViolation(4L, result3.violationLevel);
        }

        var accAfter = accountabilityService.getAccountability(4L);

        assertTrue(accAfter.getTotalViolations() - beforeTotal <= 1,
                "关闭预警后重新生成同级别预警，不应重复记录违规，期望最多+1，实际+"
                        + (accAfter.getTotalViolations() - beforeTotal));
    }
}
