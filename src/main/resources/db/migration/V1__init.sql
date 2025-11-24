-- Flyway migration: initial schema

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE professionals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    public_slug VARCHAR(255) NOT NULL UNIQUE,
    business_name VARCHAR(255),
    phone VARCHAR(100),
    timezone VARCHAR(100),
    bio TEXT,
    address VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_professional_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE professional_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT NOT NULL UNIQUE,
    allow_public_booking BOOLEAN NOT NULL DEFAULT TRUE,
    booking_advance_days INT,
    cancellation_policy_hours INT,
    default_appointment_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    email_notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    reminder_time_before_appointment_minutes INT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_settings_prof FOREIGN KEY (professional_id) REFERENCES professionals(id)
);

CREATE TABLE clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(100),
    notes TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_client_prof FOREIGN KEY (professional_id) REFERENCES professionals(id)
);
CREATE INDEX idx_clients_prof ON clients(professional_id);
CREATE UNIQUE INDEX ux_clients_email ON clients(professional_id, email);

CREATE TABLE services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL,
    price DECIMAL(10,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_service_prof FOREIGN KEY (professional_id) REFERENCES professionals(id)
);
CREATE INDEX idx_services_prof ON services(professional_id);
CREATE INDEX idx_services_active ON services(professional_id, is_active);

CREATE TABLE availability_blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    weekday TINYINT,
    specific_date DATE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_recurring BOOLEAN NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_availability_prof FOREIGN KEY (professional_id) REFERENCES professionals(id),
    CONSTRAINT ck_availability_recurring CHECK ((is_recurring = TRUE AND weekday IS NOT NULL) OR (is_recurring = FALSE AND specific_date IS NOT NULL)),
    CONSTRAINT ck_availability_time CHECK (start_time < end_time)
);
CREATE INDEX idx_availability_prof ON availability_blocks(professional_id);
CREATE UNIQUE INDEX ux_availability_recurring ON availability_blocks(professional_id, weekday, start_time, end_time);
CREATE UNIQUE INDEX ux_availability_specific ON availability_blocks(professional_id, specific_date, start_time, end_time);

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    client_id BIGINT,
    service_id BIGINT NOT NULL,
    start_datetime DATETIME(6) NOT NULL,
    end_datetime DATETIME(6) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_from_public_link BOOLEAN NOT NULL DEFAULT FALSE,
    cancellation_token VARCHAR(255) UNIQUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_appointment_prof FOREIGN KEY (professional_id) REFERENCES professionals(id),
    CONSTRAINT fk_appointment_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_appointment_service FOREIGN KEY (service_id) REFERENCES services(id),
    CONSTRAINT ck_appointment_time CHECK (start_datetime < end_datetime)
);
CREATE INDEX idx_appointments_prof_start ON appointments(professional_id, start_datetime);
CREATE INDEX idx_appointments_status ON appointments(professional_id, status, start_datetime);
CREATE INDEX idx_appointments_client ON appointments(client_id, start_datetime);

CREATE TABLE appointment_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    requested_datetime DATETIME(6) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    client_email VARCHAR(255),
    client_phone VARCHAR(100),
    status VARCHAR(32) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_request_prof FOREIGN KEY (professional_id) REFERENCES professionals(id),
    CONSTRAINT fk_request_service FOREIGN KEY (service_id) REFERENCES services(id)
);
CREATE INDEX idx_request_prof_status ON appointment_requests(professional_id, status, requested_datetime);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT NOT NULL,
    appointment_id BIGINT,
    channel VARCHAR(32) NOT NULL,
    type VARCHAR(255) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    payload JSON,
    sent_at DATETIME(6),
    status VARCHAR(32) NOT NULL,
    error_message TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_notification_prof FOREIGN KEY (professional_id) REFERENCES professionals(id),
    CONSTRAINT fk_notification_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);
CREATE INDEX idx_notifications_prof ON notifications(professional_id, sent_at);
CREATE INDEX idx_notifications_status ON notifications(status, sent_at);

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255),
    entity_id BIGINT,
    metadata JSON,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id, created_at);

CREATE TABLE password_resets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME(6) NOT NULL,
    used_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_password_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_password_user ON password_resets(user_id, expires_at);

CREATE TABLE public_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE,
    token VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(32) NOT NULL,
    expires_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_publiclink_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);
