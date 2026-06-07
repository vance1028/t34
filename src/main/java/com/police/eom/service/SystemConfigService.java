package com.police.eom.service;

import com.police.eom.domain.SystemConfig;
import com.police.eom.repo.SystemConfigRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SystemConfigService {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigService.class);

    public static final String CFG_IMMINENT_HOURS = "warning.imminent.hours";
    public static final String CFG_SEVERE_HOURS = "warning.overdue.severe_hours";
    public static final String CFG_NOTIFY_INTERVAL_MIN = "warning.notification.interval_minutes";
    public static final String CFG_RESTRICTION_THRESHOLD = "restriction.violation.threshold";
    public static final String CFG_RESTRICTION_DAYS = "restriction.default.days";
    public static final String CFG_SCANNER_INTERVAL_MS = "scheduler.scanner.interval_ms";
    public static final String CFG_LOCK_TTL_SECONDS = "scheduler.lock.ttl_seconds";

    private final SystemConfigRepository configRepo;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    private static final Map<String, String> DEFAULTS = Map.of(
            CFG_IMMINENT_HOURS, "2",
            CFG_SEVERE_HOURS, "24",
            CFG_NOTIFY_INTERVAL_MIN, "30",
            CFG_RESTRICTION_THRESHOLD, "3",
            CFG_RESTRICTION_DAYS, "30",
            CFG_SCANNER_INTERVAL_MS, "60000",
            CFG_LOCK_TTL_SECONDS, "300"
    );

    public SystemConfigService(SystemConfigRepository configRepo) {
        this.configRepo = configRepo;
    }

    @PostConstruct
    public void init() {
        refreshCache();
    }

    public void refreshCache() {
        log.info("Refreshing system config cache");
        cache.clear();
        configRepo.findAll().forEach(c -> cache.put(c.getConfigKey(), c.getConfigValue()));
        DEFAULTS.forEach((k, v) -> cache.putIfAbsent(k, v));
    }

    public String getValue(String key) {
        String val = cache.get(key);
        if (val == null) {
            val = configRepo.findByConfigKey(key)
                    .map(SystemConfig::getConfigValue)
                    .orElse(DEFAULTS.getOrDefault(key, ""));
            cache.put(key, val);
        }
        return val;
    }

    public int getInt(String key) {
        try {
            return Integer.parseInt(getValue(key));
        } catch (NumberFormatException e) {
            log.warn("Invalid integer config: {} = {}", key, getValue(key));
            return Integer.parseInt(DEFAULTS.getOrDefault(key, "0"));
        }
    }

    public long getLong(String key) {
        try {
            return Long.parseLong(getValue(key));
        } catch (NumberFormatException e) {
            log.warn("Invalid long config: {} = {}", key, getValue(key));
            return Long.parseLong(DEFAULTS.getOrDefault(key, "0"));
        }
    }

    public Duration getImminentThreshold() {
        return Duration.ofHours(getInt(CFG_IMMINENT_HOURS));
    }

    public Duration getSevereThreshold() {
        return Duration.ofHours(getInt(CFG_SEVERE_HOURS));
    }

    public Duration getNotificationInterval() {
        return Duration.ofMinutes(getInt(CFG_NOTIFY_INTERVAL_MIN));
    }

    public int getRestrictionThreshold() {
        return getInt(CFG_RESTRICTION_THRESHOLD);
    }

    public int getDefaultRestrictionDays() {
        return getInt(CFG_RESTRICTION_DAYS);
    }

    public long getScannerIntervalMs() {
        return getLong(CFG_SCANNER_INTERVAL_MS);
    }

    public int getLockTtlSeconds() {
        return getInt(CFG_LOCK_TTL_SECONDS);
    }

    public SystemConfig updateConfig(String key, String value, String description) {
        SystemConfig cfg = configRepo.findByConfigKey(key)
                .orElseGet(() -> {
                    SystemConfig c = new SystemConfig();
                    c.setConfigKey(key);
                    return c;
                });
        cfg.setConfigValue(value);
        if (description != null && !description.isBlank()) {
            cfg.setDescription(description);
        }
        SystemConfig saved = configRepo.save(cfg);
        cache.put(key, value);
        log.info("Updated config: {} = {}", key, value);
        return saved;
    }
}
