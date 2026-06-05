package com.police.eom.web;

import com.police.eom.domain.Firearm;
import com.police.eom.domain.FirearmIssuance;
import com.police.eom.service.FirearmService;
import com.police.eom.web.dto.IssueRequest;
import com.police.eom.web.dto.ReturnRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/firearms")
public class FirearmController {

    private final FirearmService service;

    public FirearmController(FirearmService service) {
        this.service = service;
    }

    @GetMapping
    public List<Firearm> list(@RequestParam(required = false) String status) {
        return service.list(status);
    }

    @GetMapping("/{id}")
    public Firearm get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<Firearm> register(@RequestBody Firearm input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(input));
    }

    @PostMapping("/{id}/issue")
    public ResponseEntity<FirearmIssuance> issue(@PathVariable Long id, @RequestBody IssueRequest req) {
        FirearmIssuance iss = service.issue(id, req.getOfficerId(), req.getPurpose(),
                req.getAmmoIssued(), req.getDueAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(iss);
    }

    @PostMapping("/{id}/return")
    public FirearmIssuance returnFirearm(@PathVariable Long id, @RequestBody(required = false) ReturnRequest req) {
        Integer ammo = req == null ? null : req.getAmmoReturned();
        return service.returnFirearm(id, ammo);
    }

    @GetMapping("/{id}/issuances")
    public List<FirearmIssuance> issuances(@PathVariable Long id) {
        return service.issuancesByFirearm(id);
    }
}
