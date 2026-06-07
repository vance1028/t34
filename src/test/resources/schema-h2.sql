-- H2 (MySQL 模式) 测试用表结构，去掉了 ENGINE/CHARSET 等 MySQL 专有子句

CREATE TABLE IF NOT EXISTS officers (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    police_no   VARCHAR(32)  NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    department  VARCHAR(128) NOT NULL DEFAULT '',
    rank_title  VARCHAR(64)  NOT NULL DEFAULT '',
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_officers_police_no UNIQUE (police_no)
);

CREATE TABLE IF NOT EXISTS evidence (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    evidence_no   VARCHAR(48)   NOT NULL,
    case_no       VARCHAR(48)   NOT NULL,
    name          VARCHAR(128)  NOT NULL,
    category      VARCHAR(32)   NOT NULL DEFAULT 'OTHER',
    description   VARCHAR(1000) NOT NULL DEFAULT '',
    status        VARCHAR(16)   NOT NULL DEFAULT 'REGISTERED',
    location      VARCHAR(128)  NOT NULL DEFAULT '',
    registered_by BIGINT        NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_evidence_no UNIQUE (evidence_no),
    CONSTRAINT fk_evidence_officer FOREIGN KEY (registered_by) REFERENCES officers (id)
);

CREATE TABLE IF NOT EXISTS custody_records (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    evidence_id  BIGINT       NOT NULL,
    action       VARCHAR(16)  NOT NULL,
    from_officer BIGINT       NULL,
    to_officer   BIGINT       NULL,
    remark       VARCHAR(500) NOT NULL DEFAULT '',
    occurred_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_custody_evidence FOREIGN KEY (evidence_id) REFERENCES evidence (id),
    CONSTRAINT fk_custody_from FOREIGN KEY (from_officer) REFERENCES officers (id),
    CONSTRAINT fk_custody_to FOREIGN KEY (to_officer) REFERENCES officers (id)
);

CREATE TABLE IF NOT EXISTS firearms (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    serial_no   VARCHAR(48)  NOT NULL,
    model       VARCHAR(64)  NOT NULL,
    type        VARCHAR(32)  NOT NULL DEFAULT 'PISTOL',
    caliber     VARCHAR(32)  NOT NULL DEFAULT '',
    status      VARCHAR(16)  NOT NULL DEFAULT 'IN_STORE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_firearm_serial UNIQUE (serial_no)
);

CREATE TABLE IF NOT EXISTS firearm_issuances (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    firearm_id    BIGINT      NOT NULL,
    officer_id    BIGINT      NOT NULL,
    purpose       VARCHAR(255) NOT NULL DEFAULT '',
    ammo_issued   INT         NOT NULL DEFAULT 0,
    ammo_returned INT         NULL,
    issued_at     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_at        TIMESTAMP   NOT NULL,
    returned_at   TIMESTAMP   NULL,
    status        VARCHAR(16) NOT NULL DEFAULT 'ISSUED',
    PRIMARY KEY (id),
    CONSTRAINT fk_issuance_firearm FOREIGN KEY (firearm_id) REFERENCES firearms (id),
    CONSTRAINT fk_issuance_officer FOREIGN KEY (officer_id) REFERENCES officers (id)
);

CREATE TABLE IF NOT EXISTS system_configs (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    config_key    VARCHAR(128) NOT NULL,
    config_value  VARCHAR(512) NOT NULL DEFAULT '',
    description   VARCHAR(255) NOT NULL DEFAULT '',
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_config_key UNIQUE (config_key)
);

CREATE TABLE IF NOT EXISTS firearm_warnings (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    issuance_id       BIGINT       NOT NULL,
    officer_id        BIGINT       NOT NULL,
    department        VARCHAR(128) NOT NULL DEFAULT '',
    warning_level     VARCHAR(16)  NOT NULL,
    status            VARCHAR(16)  NOT NULL DEFAULT 'OPEN',
    confirmed_by      BIGINT       NULL,
    confirmed_at      TIMESTAMP    NULL,
    transferred_to    BIGINT       NULL,
    transferred_at    TIMESTAMP    NULL,
    transferred_by    BIGINT       NULL,
    closed_by         BIGINT       NULL,
    closed_at         TIMESTAMP    NULL,
    close_reason      VARCHAR(500) NOT NULL DEFAULT '',
    last_notified_at  TIMESTAMP    NULL,
    escalation_count  INT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_warning_issuance FOREIGN KEY (issuance_id) REFERENCES firearm_issuances (id),
    CONSTRAINT fk_warning_officer FOREIGN KEY (officer_id) REFERENCES officers (id)
);

CREATE TABLE IF NOT EXISTS firearm_accountability (
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    officer_id            BIGINT      NOT NULL,
    overdue_count         INT         NOT NULL DEFAULT 0,
    severe_overdue_count  INT         NOT NULL DEFAULT 0,
    total_violations      INT         NOT NULL DEFAULT 0,
    has_unreturned        BOOLEAN     NOT NULL DEFAULT FALSE,
    last_violation_at     TIMESTAMP   NULL,
    created_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_accountability_officer UNIQUE (officer_id),
    CONSTRAINT fk_accountability_officer FOREIGN KEY (officer_id) REFERENCES officers (id)
);

CREATE TABLE IF NOT EXISTS firearm_issuance_restrictions (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    officer_id        BIGINT       NOT NULL,
    restriction_type  VARCHAR(32)  NOT NULL,
    reason            VARCHAR(500) NOT NULL DEFAULT '',
    restricted_by     BIGINT       NULL,
    restricted_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at        TIMESTAMP    NULL,
    status            VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    lifted_by         BIGINT       NULL,
    lifted_at         TIMESTAMP    NULL,
    lift_reason       VARCHAR(500) NOT NULL DEFAULT '',
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_restriction_officer FOREIGN KEY (officer_id) REFERENCES officers (id)
);

CREATE TABLE IF NOT EXISTS scheduler_locks (
    lock_name     VARCHAR(64)  NOT NULL,
    lock_holder   VARCHAR(128) NOT NULL,
    lock_until    TIMESTAMP    NOT NULL,
    locked_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (lock_name)
);
