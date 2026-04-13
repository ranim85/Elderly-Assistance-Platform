-- Care triage on elderly; alert accountability when resolved.

ALTER TABLE elderly_persons
    ADD COLUMN care_status VARCHAR(32) NOT NULL DEFAULT 'STABLE';

ALTER TABLE alerts
    ADD COLUMN resolved_at DATETIME(6) NULL,
    ADD COLUMN resolved_by BIGINT NULL;

ALTER TABLE alerts
    ADD CONSTRAINT fk_alerts_resolved_by FOREIGN KEY (resolved_by) REFERENCES users (id);
