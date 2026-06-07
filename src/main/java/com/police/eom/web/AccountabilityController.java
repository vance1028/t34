package com.police.eom.web;

import com.police.eom.domain.FirearmAccountability;
import com.police.eom.domain.FirearmIssuanceRestriction;
import com.police.eom.service.AccountabilityService;
import com.police.eom.web.dto.RestrictionRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accountability")
public class AccountabilityController {

    private final AccountabilityService accountabilityService;

    public AccountabilityController(AccountabilityService accountabilityService) {
        this.accountabilityService = accountabilityService;
    }

    @GetMapping("/officers")
    public List<FirearmAccountability> listAll() {
        return accountabilityService.listAllAccountability();
    }

    @GetMapping("/officers/{officerId}")
    public FirearmAccountability getByOfficer(@PathVariable Long officerId) {
        return accountabilityService.getAccountability(officerId);
    }

    @GetMapping("/officers/{officerId}/restricted")
    public boolean isRestricted(@PathVariable Long officerId) {
        return accountabilityService.isOfficerRestricted(officerId);
    }

    @GetMapping("/officers/{officerId}/restriction")
    public Optional<FirearmIssuanceRestriction> getActiveRestriction(@PathVariable Long officerId) {
        return accountabilityService.getActiveRestriction(officerId);
    }

    @GetMapping("/officers/{officerId}/restriction-history")
    public List<FirearmIssuanceRestriction> getRestrictionHistory(@PathVariable Long officerId) {
        return accountabilityService.getRestrictionHistory(officerId);
    }

    @GetMapping("/restrictions")
    public List<FirearmIssuanceRestriction> listRestrictions(@RequestParam(required = false) String status) {
        return accountabilityService.listAllRestrictions(status);
    }

    @PostMapping("/restrictions")
    public FirearmIssuanceRestriction createRestriction(@RequestBody RestrictionRequest req) {
        return accountabilityService.createRestriction(
                req.getOfficerId(),
                req.getRestrictionType(),
                req.getReason(),
                req.getOperatorId()
        );
    }

    @PostMapping("/restrictions/{id}/lift")
    public FirearmIssuanceRestriction liftRestriction(@PathVariable Long id, @RequestBody RestrictionRequest req) {
        return accountabilityService.liftRestriction(id, req.getOperatorId(), req.getReason());
    }
}
