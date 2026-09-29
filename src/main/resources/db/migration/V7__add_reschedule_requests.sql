-- Reprogramación de citas (HU-020 / HU-021).
-- Idempotente a propósito: los volúmenes creados desde database/reference/db.sql registran
-- baseline Flyway 4 y ya contienen reschedule_request_statuses y reschedule_requests, mientras
-- que la línea V1-V6 no las tiene. La columna de retención se añade consultando information_schema.
-- En la línea V1-V6 los identificadores de usuario son BIGINT con signo; el esquema de referencia
-- usa BIGINT UNSIGNED y conserva su propia definición porque CREATE TABLE IF NOT EXISTS no la toca.

CREATE TABLE IF NOT EXISTS reschedule_request_statuses (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL,
    is_terminal BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

INSERT INTO reschedule_request_statuses (id, code, name, is_terminal) VALUES
    (1, 'PENDING', 'Pendiente', FALSE),
    (2, 'APPROVED', 'Aprobada', TRUE),
    (3, 'REJECTED', 'Rechazada', TRUE),
    (4, 'CANCELLED', 'Cancelada por el usuario', TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name), is_terminal = VALUES(is_terminal);

CREATE TABLE IF NOT EXISTS reschedule_requests (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT UNSIGNED NOT NULL,
    requested_by_user_id BIGINT NOT NULL,
    requested_location_id SMALLINT UNSIGNED NOT NULL,
    status_id SMALLINT UNSIGNED NOT NULL,
    previous_start_at DATETIME NOT NULL,
    previous_end_at DATETIME NOT NULL,
    requested_start_at DATETIME NOT NULL,
    requested_end_at DATETIME NOT NULL,
    decision_reason VARCHAR(500) NULL,
    decided_by_user_id BIGINT NULL,
    decided_at DATETIME NULL,
    patient_action_after_rejection VARCHAR(30) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_reschedule_time
        CHECK (requested_end_at > requested_start_at),
    CONSTRAINT ck_reschedule_patient_action
        CHECK (
            patient_action_after_rejection IS NULL
            OR patient_action_after_rejection IN ('KEEP_APPOINTMENT', 'CANCEL_APPOINTMENT')
        ),
    CONSTRAINT fk_reschedule_appointment
        FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_reschedule_requested_by
        FOREIGN KEY (requested_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reschedule_location
        FOREIGN KEY (requested_location_id) REFERENCES locations(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reschedule_status
        FOREIGN KEY (status_id) REFERENCES reschedule_request_statuses(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reschedule_decided_by
        FOREIGN KEY (decided_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX ix_reschedule_appointment (appointment_id),
    INDEX ix_reschedule_status (status_id)
) ENGINE=InnoDB;

-- Retención provisional aprobada (DEC-005): un slot está libre solo cuando appointment_id
-- y reschedule_request_id son NULL. La columna no existe en el esquema de referencia.
SET @add_column := (
    SELECT COUNT(*) = 0 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'professional_slots' AND column_name = 'reschedule_request_id'
);
SET @statement := IF(@add_column,
    'ALTER TABLE professional_slots ADD COLUMN reschedule_request_id BIGINT UNSIGNED NULL',
    'SELECT 1');
PREPARE add_column_statement FROM @statement;
EXECUTE add_column_statement;
DEALLOCATE PREPARE add_column_statement;

SET @add_constraint := (
    SELECT COUNT(*) = 0 FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE() AND table_name = 'professional_slots'
      AND constraint_name = 'fk_slots_reschedule_request'
);
SET @statement := IF(@add_constraint,
    'ALTER TABLE professional_slots ADD CONSTRAINT fk_slots_reschedule_request FOREIGN KEY (reschedule_request_id) REFERENCES reschedule_requests(id) ON DELETE SET NULL',
    'SELECT 1');
PREPARE add_constraint_statement FROM @statement;
EXECUTE add_constraint_statement;
DEALLOCATE PREPARE add_constraint_statement;
