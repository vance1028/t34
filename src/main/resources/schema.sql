-- 公安证据与枪械管理平台 - 表结构（MySQL）

CREATE TABLE IF NOT EXISTS officers (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    police_no   VARCHAR(32)  NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    department  VARCHAR(128) NOT NULL DEFAULT '',
    rank_title  VARCHAR(64)  NOT NULL DEFAULT '',
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_officers_police_no (police_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS evidence (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    evidence_no  VARCHAR(48)  NOT NULL,
    case_no      VARCHAR(48)  NOT NULL,
    name         VARCHAR(128) NOT NULL,
    category     VARCHAR(32)  NOT NULL DEFAULT 'OTHER',
    description  VARCHAR(1000) NOT NULL DEFAULT '',
    status       VARCHAR(16)  NOT NULL DEFAULT 'REGISTERED',
    location     VARCHAR(128) NOT NULL DEFAULT '',
    registered_by BIGINT      NULL,
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_evidence_no (evidence_no),
    KEY idx_evidence_case (case_no),
    KEY idx_evidence_status (status),
    CONSTRAINT fk_evidence_officer FOREIGN KEY (registered_by) REFERENCES officers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS custody_records (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    evidence_id  BIGINT       NOT NULL,
    action       VARCHAR(16)  NOT NULL,
    from_officer BIGINT       NULL,
    to_officer   BIGINT       NULL,
    remark       VARCHAR(500) NOT NULL DEFAULT '',
    occurred_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_custody_evidence (evidence_id),
    CONSTRAINT fk_custody_evidence FOREIGN KEY (evidence_id) REFERENCES evidence (id),
    CONSTRAINT fk_custody_from FOREIGN KEY (from_officer) REFERENCES officers (id),
    CONSTRAINT fk_custody_to FOREIGN KEY (to_officer) REFERENCES officers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS firearms (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    serial_no   VARCHAR(48)  NOT NULL,
    model       VARCHAR(64)  NOT NULL,
    type        VARCHAR(32)  NOT NULL DEFAULT 'PISTOL',
    caliber     VARCHAR(32)  NOT NULL DEFAULT '',
    status      VARCHAR(16)  NOT NULL DEFAULT 'IN_STORE',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_firearm_serial (serial_no),
    KEY idx_firearm_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS firearm_issuances (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    firearm_id    BIGINT      NOT NULL,
    officer_id    BIGINT      NOT NULL,
    purpose       VARCHAR(255) NOT NULL DEFAULT '',
    ammo_issued   INT         NOT NULL DEFAULT 0,
    ammo_returned INT         NULL,
    issued_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    due_at        DATETIME(3) NOT NULL,
    returned_at   DATETIME(3) NULL,
    status        VARCHAR(16) NOT NULL DEFAULT 'ISSUED',
    PRIMARY KEY (id),
    KEY idx_issuance_firearm (firearm_id),
    KEY idx_issuance_officer (officer_id),
    KEY idx_issuance_status (status),
    KEY idx_issuance_due_status (due_at, status),
    CONSTRAINT fk_issuance_firearm FOREIGN KEY (firearm_id) REFERENCES firearms (id),
    CONSTRAINT fk_issuance_officer FOREIGN KEY (officer_id) REFERENCES officers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS system_configs (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    config_key    VARCHAR(128) NOT NULL,
    config_value  VARCHAR(512) NOT NULL DEFAULT '',
    description   VARCHAR(255) NOT NULL DEFAULT '',
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS firearm_warnings (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    issuance_id       BIGINT       NOT NULL,
    officer_id        BIGINT       NOT NULL,
    department        VARCHAR(128) NOT NULL DEFAULT '',
    warning_level     VARCHAR(16)  NOT NULL,
    status            VARCHAR(16)  NOT NULL DEFAULT 'OPEN',
    confirmed_by      BIGINT       NULL,
    confirmed_at      DATETIME(3)  NULL,
    transferred_to    BIGINT       NULL,
    transferred_at    DATETIME(3)  NULL,
    transferred_by    BIGINT       NULL,
    closed_by         BIGINT       NULL,
    closed_at         DATETIME(3)  NULL,
    close_reason      VARCHAR(500) NOT NULL DEFAULT '',
    last_notified_at  DATETIME(3)  NULL,
    escalation_count  INT          NOT NULL DEFAULT 0,
    created_at        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_warning_issuance (issuance_id),
    KEY idx_warning_officer (officer_id),
    KEY idx_warning_status (status),
    KEY idx_warning_level (warning_level),
    UNIQUE KEY uk_warning_active_issuance (issuance_id, status),
    CONSTRAINT fk_warning_issuance FOREIGN KEY (issuance_id) REFERENCES firearm_issuances (id),
    CONSTRAINT fk_warning_officer FOREIGN KEY (officer_id) REFERENCES officers (id),
    CONSTRAINT fk_warning_confirmed_by FOREIGN KEY (confirmed_by) REFERENCES officers (id),
    CONSTRAINT fk_warning_transferred_to FOREIGN KEY (transferred_to) REFERENCES officers (id),
    CONSTRAINT fk_warning_transferred_by FOREIGN KEY (transferred_by) REFERENCES officers (id),
    CONSTRAINT fk_warning_closed_by FOREIGN KEY (closed_by) REFERENCES officers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS firearm_accountability (
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    officer_id            BIGINT      NOT NULL,
    overdue_count         INT         NOT NULL DEFAULT 0,
    severe_overdue_count  INT         NOT NULL DEFAULT 0,
    total_violations      INT         NOT NULL DEFAULT 0,
    has_unreturned        TINYINT(1)  NOT NULL DEFAULT 0,
    last_violation_at     DATETIME(3) NULL,
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_accountability_officer (officer_id),
    CONSTRAINT fk_accountability_officer FOREIGN KEY (officer_id) REFERENCES officers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS firearm_issuance_restrictions (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    officer_id        BIGINT       NOT NULL,
    restriction_type  VARCHAR(32)  NOT NULL,
    reason            VARCHAR(500) NOT NULL DEFAULT '',
    restricted_by     BIGINT       NULL,
    restricted_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    expires_at        DATETIME(3)  NULL,
    status            VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    lifted_by         BIGINT       NULL,
    lifted_at         DATETIME(3)  NULL,
    lift_reason       VARCHAR(500) NOT NULL DEFAULT '',
    created_at        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_restriction_officer (officer_id),
    KEY idx_restriction_status (status),
    CONSTRAINT fk_restriction_officer FOREIGN KEY (officer_id) REFERENCES officers (id),
    CONSTRAINT fk_restriction_by FOREIGN KEY (restricted_by) REFERENCES officers (id),
    CONSTRAINT fk_restriction_lifted_by FOREIGN KEY (lifted_by) REFERENCES officers (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS scheduler_locks (
    lock_name     VARCHAR(64)  NOT NULL,
    lock_holder   VARCHAR(128) NOT NULL,
    lock_until    DATETIME(3)  NOT NULL,
    locked_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (lock_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
