CREATE TABLE monitor
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name             VARCHAR(100)  NOT NULL,
    url              VARCHAR(2048) NOT NULL,
    method           VARCHAR(10)   NOT NULL DEFAULT 'GET',
    interval_seconds INTEGER       NOT NULL DEFAULT 60,
    timeout_ms       INTEGER       NOT NULL DEFAULT 5000,
    expected_status  INTEGER       NOT NULL DEFAULT 200,
    enabled          BOOLEAN       NOT NULL DEFAULT TRUE,
    status           VARCHAR(10)   NOT NULL DEFAULT 'UNKNOWN',
    last_checked_at  TIMESTAMPTZ,
    created_at       TIMESTAMPTZ   NOT NULL,
    updated_at       TIMESTAMPTZ   NOT NULL,

    CONSTRAINT uk_monitor_name UNIQUE (name),
    CONSTRAINT ck_monitor_method CHECK (method IN ('GET', 'HEAD')),
    CONSTRAINT ck_monitor_status CHECK (status IN ('UNKNOWN', 'UP', 'DOWN', 'PAUSED')),
    CONSTRAINT ck_monitor_interval_seconds CHECK (interval_seconds BETWEEN 30 AND 3600),
    CONSTRAINT ck_monitor_timeout_ms CHECK (timeout_ms BETWEEN 500 AND 30000)
);
