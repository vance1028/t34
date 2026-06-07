package com.police.eom.web;

import com.police.eom.domain.FirearmWarning;
import com.police.eom.service.WarningService;
import com.police.eom.web.dto.WarningActionRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warnings")
public class WarningController {

    private final WarningService warningService;

    public WarningController(WarningService warningService) {
        this.warningService = warningService;
    }

    @GetMapping
    public List<FirearmWarning> list(@RequestParam(required = false) String status,
                                     @RequestParam(required = false) Long officerId) {
        return warningService.list(status, officerId);
    }

    @GetMapping("/active")
    public List<FirearmWarning> listActive() {
        return warningService.listActive();
    }

    @GetMapping("/{id}")
    public FirearmWarning get(@PathVariable Long id) {
        return warningService.get(id);
    }

    @PostMapping("/{id}/confirm")
    public FirearmWarning confirm(@PathVariable Long id, @RequestBody WarningActionRequest req) {
        return warningService.confirm(id, req.getOperatorId());
    }

    @PostMapping("/{id}/transfer")
    public FirearmWarning transfer(@PathVariable Long id, @RequestBody WarningActionRequest req) {
        return warningService.transfer(id, req.getTargetOfficerId(), req.getOperatorId(), req.getReason());
    }

    @PostMapping("/{id}/close")
    public FirearmWarning close(@PathVariable Long id, @RequestBody WarningActionRequest req) {
        return warningService.close(id, req.getOperatorId(), req.getReason());
    }
}
