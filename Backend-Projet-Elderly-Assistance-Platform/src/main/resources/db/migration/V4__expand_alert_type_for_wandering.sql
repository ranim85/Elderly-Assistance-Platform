-- Allow newer alert enum values (e.g. WANDERING_EMERGENCY) on existing databases.
-- Some legacy schemas used a restrictive ENUM for alerts.alert_type.

ALTER TABLE alerts
    MODIFY COLUMN alert_type VARCHAR(255) NULL;
