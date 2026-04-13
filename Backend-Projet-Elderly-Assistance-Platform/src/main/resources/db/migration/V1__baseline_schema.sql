-- Baseline schema for Elderly Assistance (MySQL 8+).
-- Run against an empty database; Flyway records this migration once.

CREATE TABLE elderly_persons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    date_of_birth DATE,
    address VARCHAR(255),
    medical_conditions VARCHAR(255),
    caregiver_id BIGINT NULL
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(120),
    role VARCHAR(32) NOT NULL,
    linked_elderly_person_id BIGINT NULL,
    CONSTRAINT fk_users_linked_elderly FOREIGN KEY (linked_elderly_person_id) REFERENCES elderly_persons (id)
);

ALTER TABLE elderly_persons
    ADD CONSTRAINT fk_elderly_caregiver FOREIGN KEY (caregiver_id) REFERENCES users (id);

CREATE TABLE health_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_pressure VARCHAR(255),
    heart_rate INT,
    blood_sugar DOUBLE,
    recorded_at DATETIME(6),
    elderly_id BIGINT NOT NULL,
    CONSTRAINT fk_health_elderly FOREIGN KEY (elderly_id) REFERENCES elderly_persons (id)
);

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_name VARCHAR(255),
    purpose VARCHAR(255),
    appointment_date DATETIME(6),
    status VARCHAR(255),
    elderly_id BIGINT NOT NULL,
    CONSTRAINT fk_appt_elderly FOREIGN KEY (elderly_id) REFERENCES elderly_persons (id)
);

CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_type VARCHAR(255),
    priority VARCHAR(255),
    description TEXT,
    alert_timestamp DATETIME(6),
    is_resolved TINYINT(1) NOT NULL DEFAULT 0,
    elderly_id BIGINT NOT NULL,
    CONSTRAINT fk_alert_elderly FOREIGN KEY (elderly_id) REFERENCES elderly_persons (id)
);

CREATE INDEX idx_alerts_alert_timestamp ON alerts (alert_timestamp);
CREATE INDEX idx_alerts_elderly ON alerts (elderly_id);
CREATE INDEX idx_health_elderly ON health_records (elderly_id);
CREATE INDEX idx_users_email ON users (email);
