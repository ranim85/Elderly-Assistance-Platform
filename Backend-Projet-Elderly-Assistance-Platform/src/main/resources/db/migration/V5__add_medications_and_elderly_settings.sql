-- Add tables required by medication planner and geofencing modules.

CREATE TABLE IF NOT EXISTS elderly_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    elderly_id BIGINT NOT NULL UNIQUE,
    home_latitude DOUBLE NULL,
    home_longitude DOUBLE NULL,
    safe_zone_radius DOUBLE NULL,
    CONSTRAINT fk_elderly_settings_elderly
        FOREIGN KEY (elderly_id) REFERENCES elderly_persons (id)
);

CREATE TABLE IF NOT EXISTS medications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dosage VARCHAR(255) NULL,
    scheduled_time DATETIME(6) NOT NULL,
    is_taken TINYINT(1) NOT NULL DEFAULT 0,
    time_taken DATETIME(6) NULL,
    elderly_id BIGINT NOT NULL,
    CONSTRAINT fk_medications_elderly
        FOREIGN KEY (elderly_id) REFERENCES elderly_persons (id)
);

CREATE INDEX idx_medications_elderly ON medications (elderly_id);
CREATE INDEX idx_medications_scheduled_time ON medications (scheduled_time);
