package com.police.eom.web;

import com.police.eom.domain.CustodyRecord;
import com.police.eom.domain.Evidence;
import com.police.eom.service.EvidenceService;
import com.police.eom.web.dto.CustodyActionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService service;

    public EvidenceController(EvidenceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Evidence> list(@RequestParam(required = false) String caseNo,
                               @RequestParam(required = false) String status) {
        return service.list(caseNo, status);
    }

    @GetMapping("/{id}")
    public Evidence get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<Evidence> register(@RequestBody Evidence input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(input));
    }

    @PostMapping("/{id}/checkout")
    public Evidence checkOut(@PathVariable Long id, @RequestBody CustodyActionRequest req) {
        return service.checkOut(id, req.getOfficerId(), req.getRemark());
    }

    @PostMapping("/{id}/checkin")
    public Evidence checkIn(@PathVariable Long id, @RequestBody CustodyActionRequest req) {
        return service.checkIn(id, req.getOfficerId(), req.getRemark());
    }

    @GetMapping("/{id}/custody")
    public List<CustodyRecord> custody(@PathVariable Long id) {
        return service.custodyChain(id);
    }
}
