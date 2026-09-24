-- Evolución del esquema UUID de autenticación hacia la agenda normalizada de S3.
-- No modifica las migraciones V1/V2 ni el esquema de referencia del laboratorio.

CREATE TABLE locations (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(180) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE specialties (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL UNIQUE,
    appointment_duration_minutes SMALLINT UNSIGNED NOT NULL,
    is_general BOOLEAN NOT NULL DEFAULT FALSE,
    requires_admin_approval BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_specialty_duration CHECK (appointment_duration_minutes IN (30, 60))
) ENGINE=InnoDB;

CREATE TABLE professionals (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    professional_code VARCHAR(40) NOT NULL UNIQUE,
    license_number VARCHAR(80) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_professionals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE professional_specialties (
    professional_id BIGINT UNSIGNED NOT NULL,
    specialty_id SMALLINT UNSIGNED NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (professional_id, specialty_id),
    CONSTRAINT fk_professional_specialty_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE,
    CONSTRAINT fk_professional_specialty_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE professional_locations (
    professional_id BIGINT UNSIGNED NOT NULL,
    location_id SMALLINT UNSIGNED NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (professional_id, location_id),
    CONSTRAINT fk_professional_location_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE,
    CONSTRAINT fk_professional_location_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE appointment_statuses (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE appointments (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    patient_user_id BIGINT NOT NULL,
    professional_id BIGINT UNSIGNED NOT NULL,
    location_id SMALLINT UNSIGNED NOT NULL,
    specialty_id SMALLINT UNSIGNED NOT NULL,
    status_id SMALLINT UNSIGNED NOT NULL,
    scheduled_start_at DATETIME NOT NULL,
    scheduled_end_at DATETIME NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    approved_by_user_id BIGINT NULL,
    approved_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT ck_appointment_time CHECK (scheduled_end_at > scheduled_start_at),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_specialty FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_status FOREIGN KEY (status_id) REFERENCES appointment_statuses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointments_approved_by FOREIGN KEY (approved_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX ix_appointments_status (status_id),
    INDEX ix_appointments_professional (professional_id, scheduled_start_at)
) ENGINE=InnoDB;

CREATE TABLE availability_blocks (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    professional_id BIGINT UNSIGNED NOT NULL,
    location_id SMALLINT UNSIGNED NOT NULL,
    available_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_availability_block_time CHECK (end_time > start_time),
    CONSTRAINT fk_availability_professional FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE RESTRICT,
    CONSTRAINT fk_availability_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    INDEX ix_availability_prof_date (professional_id, available_date, start_time),
    INDEX ix_availability_location_date (location_id, available_date)
) ENGINE=InnoDB;

CREATE TABLE professional_slots (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    availability_block_id BIGINT UNSIGNED NOT NULL,
    start_at DATETIME NOT NULL,
    end_at DATETIME NOT NULL,
    appointment_id BIGINT UNSIGNED NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_professional_slot_time CHECK (end_at > start_at),
    CONSTRAINT uq_block_slot UNIQUE (availability_block_id, start_at),
    CONSTRAINT fk_slots_availability_block FOREIGN KEY (availability_block_id) REFERENCES availability_blocks(id) ON DELETE RESTRICT,
    CONSTRAINT fk_slots_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    INDEX ix_slots_start (start_at),
    INDEX ix_slots_appointment (appointment_id)
) ENGINE=InnoDB;

CREATE TABLE appointment_status_history (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT UNSIGNED NOT NULL,
    status_id SMALLINT UNSIGNED NOT NULL,
    changed_by_user_id BIGINT NULL,
    change_source VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_status_history_source CHECK (change_source IN ('SYSTEM', 'USER', 'ADMIN')),
    CONSTRAINT fk_status_history_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_status_history_status FOREIGN KEY (status_id) REFERENCES appointment_statuses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_status_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX ix_status_history_appointment (appointment_id, changed_at)
) ENGINE=InnoDB;

INSERT INTO locations (id, code, name, address, city, department, active) VALUES
    (1, 'HIC', 'Hospital Internacional de Colombia (HIC)', 'Km 7 Autopista Bucaramanga - Piedecuesta, Valle de Menzulí', 'Piedecuesta', 'Santander', TRUE),
    (2, 'ICV', 'Fundación Cardiovascular de Colombia - Instituto Cardiovascular (ICV)', 'Calle 155A No. 23-58, Urbanización El Bosque', 'Floridablanca', 'Santander', TRUE);

INSERT INTO specialties (id, code, name, appointment_duration_minutes, is_general, requires_admin_approval, active) VALUES
    (1, 'MEDICINA_GENERAL', 'Medicina General', 30, TRUE, FALSE, TRUE),
    (2, 'CARDIOLOGIA_ADULTO', 'Cardiología Adulto', 30, FALSE, TRUE, TRUE),
    (3, 'ORTOPEDIA_TRAUMATOLOGIA', 'Ortopedia y Traumatología', 60, FALSE, TRUE, TRUE);

INSERT INTO appointment_statuses (id, code, name, is_terminal) VALUES
    (1, 'REQUESTED', 'Solicitada / pendiente de aprobación', FALSE),
    (2, 'APPROVED', 'Aprobada', FALSE),
    (3, 'REJECTED', 'Rechazada', TRUE),
    (4, 'CANCELLED', 'Cancelada', TRUE),
    (5, 'COMPLETED', 'Atendida / completada', TRUE),
    (6, 'NO_SHOW', 'No asistió', TRUE);
