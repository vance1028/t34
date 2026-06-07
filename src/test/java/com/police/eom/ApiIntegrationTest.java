package com.police.eom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @Test
    void health_ok() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void officers_seeded() throws Exception {
        mvc.perform(get("/api/officers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void officer_duplicate_policeNo_conflict() throws Exception {
        String body = "{\"policeNo\":\"030001\",\"name\":\"重复警号\"}";
        mvc.perform(post("/api/officers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void officer_blank_name_badRequest() throws Exception {
        String body = "{\"policeNo\":\"099999\",\"name\":\"\"}";
        mvc.perform(post("/api/officers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void evidence_list_byCase() throws Exception {
        mvc.perform(get("/api/evidence").param("caseNo", "AJ-2026-0101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void evidence_register_creates_custody_register_record() throws Exception {
        String body = "{\"evidenceNo\":\"WZ-TEST-9001\",\"caseNo\":\"AJ-TEST\",\"name\":\"测试物证\",\"category\":\"OTHER\",\"registeredBy\":3}";
        String resp = mvc.perform(post("/api/evidence").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_STORAGE"))
                .andReturn().getResponse().getContentAsString();
        long id = om.readTree(resp).get("id").asLong();

        mvc.perform(get("/api/evidence/" + id + "/custody"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].action").value("REGISTER"));
    }

    @Test
    void evidence_duplicate_no_conflict() throws Exception {
        String body = "{\"evidenceNo\":\"WZ-2026-0001\",\"caseNo\":\"X\",\"name\":\"dup\"}";
        mvc.perform(post("/api/evidence").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void evidence_checkout_then_checkin_chain() throws Exception {
        // evidence 1 是 IN_STORAGE
        mvc.perform(post("/api/evidence/1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"officerId\":1,\"remark\":\"勘验借出\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));

        // 再次借出应 409
        mvc.perform(post("/api/evidence/1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"officerId\":1}"))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/evidence/1/checkin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"officerId\":3,\"remark\":\"归还入库\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_STORAGE"));

        // 链路应包含 REGISTER + CHECK_OUT + CHECK_IN
        mvc.perform(get("/api/evidence/1/custody"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void evidence_checkin_without_checkout_conflict() throws Exception {
        // evidence 2 处于 IN_STORAGE，未借出
        mvc.perform(post("/api/evidence/2/checkin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"officerId\":3}"))
                .andExpect(status().isConflict());
    }

    @Test
    void firearm_issue_and_return_flow() throws Exception {
        // firearm 1 在库
        String due = LocalDateTime.now().plusHours(6).toString();
        String issueBody = "{\"officerId\":3,\"purpose\":\"巡逻\",\"ammoIssued\":15,\"dueAt\":\"" + due + "\"}";
        mvc.perform(post("/api/firearms/1/issue").contentType(MediaType.APPLICATION_JSON).content(issueBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ISSUED"));

        // 枪应变为 ISSUED
        mvc.perform(get("/api/firearms/1"))
                .andExpect(jsonPath("$.status").value("ISSUED"));

        // 再次领用应 409
        mvc.perform(post("/api/firearms/1/issue").contentType(MediaType.APPLICATION_JSON).content(issueBody))
                .andExpect(status().isConflict());

        // 归还
        mvc.perform(post("/api/firearms/1/return").contentType(MediaType.APPLICATION_JSON).content("{\"ammoReturned\":13}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        mvc.perform(get("/api/firearms/1"))
                .andExpect(jsonPath("$.status").value("IN_STORE"));
    }

    @Test
    void firearm_issue_pastDue_badRequest() throws Exception {
        String due = LocalDateTime.now().minusHours(1).toString();
        String issueBody = "{\"officerId\":3,\"ammoIssued\":10,\"dueAt\":\"" + due + "\"}";
        mvc.perform(post("/api/firearms/3/issue").contentType(MediaType.APPLICATION_JSON).content(issueBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void firearm_return_ammo_exceeds_issued_badRequest() throws Exception {
        // firearm 2 在种子里是 ISSUED（issuance id=1, ammo_issued=15）
        mvc.perform(post("/api/firearms/2/return").contentType(MediaType.APPLICATION_JSON).content("{\"ammoReturned\":99}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void firearm_return_not_issued_conflict() throws Exception {
        // firearm 3 在库，未领用
        mvc.perform(post("/api/firearms/3/return").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void evidence_not_found() throws Exception {
        mvc.perform(get("/api/evidence/99999"))
                .andExpect(status().isNotFound());
    }
}
