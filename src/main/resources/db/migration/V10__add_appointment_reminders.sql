-- HU-028 (DEC-010): rastro de recordatorios y deduplicación por (cita, franja).
-- dedup_key vale 1 solo para SENT; FAILED lo deja NULL, así el índice único impide un segundo SENT
-- para la misma franja pero permite reintentos tras un fallo (los NULL no colisionan en MySQL).
CREATE TABLE appointment_reminders (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT UNSIGNED NOT NULL,
    window_start_at DATETIME NOT NULL,
    status VARCHAR(10) NOT NULL,
    dedup_key TINYINT NULL,
    detail VARCHAR(500) NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_reminder_status CHECK (status IN ('SENT', 'FAILED')),
    CONSTRAINT ck_reminder_dedup CHECK ((status = 'SENT' AND dedup_key = 1) OR (status = 'FAILED' AND dedup_key IS NULL)),
    CONSTRAINT uq_reminder_sent UNIQUE (appointment_id, window_start_at, dedup_key),
    CONSTRAINT fk_reminder_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    INDEX ix_reminders_appointment (appointment_id)
) ENGINE=InnoDB;
