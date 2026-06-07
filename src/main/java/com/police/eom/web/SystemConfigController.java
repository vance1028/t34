package com.police.eom.web;

import com.police.eom.domain.SystemConfig;
import com.police.eom.service.SystemConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/configs")
public class SystemConfigController {

    private final SystemConfigService configService;

    public SystemConfigController(SystemConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public Map<String, String> getAll() {
        return Map.of(
                SystemConfigService.CFG_IMMINENT_HOURS, String.valueOf(configService.getInt(SystemConfigService.CFG_IMMINENT_HOURS)),
                SystemConfigService.CFG_SEVERE_HOURS, String.valueOf(configService.getInt(SystemConfigService.CFG_SEVERE_HOURS)),
                SystemConfigService.CFG_NOTIFY_INTERVAL_MIN, String.valueOf(configService.getInt(SystemConfigService.CFG_NOTIFY_INTERVAL_MIN)),
                SystemConfigService.CFG_RESTRICTION_THRESHOLD, String.valueOf(configService.getInt(SystemConfigService.CFG_RESTRICTION_THRESHOLD)),
                SystemConfigService.CFG_RESTRICTION_DAYS, String.valueOf(configService.getInt(SystemConfigService.CFG_RESTRICTION_DAYS)),
                SystemConfigService.CFG_SCANNER_INTERVAL_MS, String.valueOf(configService.getLong(SystemConfigService.CFG_SCANNER_INTERVAL_MS)),
                SystemConfigService.CFG_LOCK_TTL_SECONDS, String.valueOf(configService.getInt(SystemConfigService.CFG_LOCK_TTL_SECONDS))
        );
    }

    @PutMapping("/{key}")
    public SystemConfig update(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        String description = body.get("description");
        return configService.updateConfig(key, value, description);
    }

    @PostMapping("/refresh")
    public void refresh() {
        configService.refreshCache();
    }
}
