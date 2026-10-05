-- Datos semilla SINTÉTICOS para probar la web (documentados en DATOS_SEMILLA.md, raíz del proyecto).
-- Contraseña de laboratorio de TODAS las cuentas: Demo1234*  (BCrypt, solo entrenamiento).
-- Idempotente y válida en las dos líneas de BD (Flyway V1-V10 y database/reference/db.sql):
--  * roles por `code` (los ids difieren entre ambas líneas);
--  * personas/profesionales/EPS por email o código con NOT EXISTS;
--  * documentos 93xxxxxxxx para no chocar con los de V4 ni los de db.sql (900000001, 91..., 92...);
--  * solo columnas comunes a ambas líneas (sin email_verified ni insurance_affiliation_id).
-- Las fechas son relativas a la fecha de aplicación; ver "Refrescar fechas" en DATOS_SEMILLA.md.

-- ---------------------------------------------------------------- usuarios
INSERT INTO users (first_name, last_name, document_type, document_number, email, phone, password_hash, active)
SELECT s.first_name, s.last_name, 'CC', s.document_number, s.email, s.phone,
       '$2y$10$QAPT/bPvvEILB0ovqykfTuwSBznwY2p0rJhZJguneKjk2dr7VQFeG', s.active
FROM (
    SELECT 'Admin' first_name, 'Laboratorio' last_name, '9300000001' document_number, 'admin@demo.invalid' email, '3000000001' phone, TRUE active
    UNION ALL SELECT 'Admin', 'Suplente', '9300000002', 'admin2@demo.invalid', '3000000002', TRUE
    UNION ALL SELECT 'Multi', 'Rol', '9300000003', 'multirol@demo.invalid', '3000000003', TRUE
    UNION ALL SELECT 'Paula', 'Gómez', '9300000101', 'dra.gomez@demo.invalid', '3100000101', TRUE
    UNION ALL SELECT 'Esteban', 'Salazar', '9300000102', 'dr.salazar@demo.invalid', '3100000102', TRUE
    UNION ALL SELECT 'Olga', 'Ortiz', '9300000103', 'dra.ortiz@demo.invalid', '3100000103', TRUE
    UNION ALL SELECT 'Ignacio', 'Inactivo', '9300000104', 'dr.inactivo@demo.invalid', '3100000104', TRUE
    UNION ALL SELECT 'Nora', 'Nueva', '9300000201', 'paciente.nuevo@demo.invalid', '3200000201', TRUE
    UNION ALL SELECT 'Gabriel', 'General', '9300000202', 'paciente.general@demo.invalid', '3200000202', TRUE
    UNION ALL SELECT 'Elena', 'Especialista', '9300000203', 'paciente.especialista@demo.invalid', '3200000203', TRUE
    UNION ALL SELECT 'Hugo', 'Historial', '9300000204', 'paciente.historial@demo.invalid', '3200000204', TRUE
    UNION ALL SELECT 'Rita', 'Reprogramación', '9300000205', 'paciente.reprogramacion@demo.invalid', '3200000205', TRUE
    UNION ALL SELECT 'Rafael', 'Recordatorio', '9300000206', 'paciente.recordatorio@demo.invalid', '3200000206', TRUE
    UNION ALL SELECT 'Ines', 'Inactiva', '9300000207', 'paciente.inactivo@demo.invalid', '3200000207', FALSE
) s
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = s.email);

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM (
    SELECT 'admin@demo.invalid' email, 'ADMIN' code
    UNION ALL SELECT 'admin2@demo.invalid', 'ADMIN'
    UNION ALL SELECT 'multirol@demo.invalid', 'ADMIN'
    UNION ALL SELECT 'multirol@demo.invalid', 'PROFESSIONAL'
    UNION ALL SELECT 'multirol@demo.invalid', 'USER'
    UNION ALL SELECT 'dra.gomez@demo.invalid', 'PROFESSIONAL'
    UNION ALL SELECT 'dr.salazar@demo.invalid', 'PROFESSIONAL'
    UNION ALL SELECT 'dra.ortiz@demo.invalid', 'PROFESSIONAL'
    UNION ALL SELECT 'dr.inactivo@demo.invalid', 'PROFESSIONAL'
    UNION ALL SELECT 'paciente.nuevo@demo.invalid', 'USER'
    UNION ALL SELECT 'paciente.general@demo.invalid', 'USER'
    UNION ALL SELECT 'paciente.especialista@demo.invalid', 'USER'
    UNION ALL SELECT 'paciente.historial@demo.invalid', 'USER'
    UNION ALL SELECT 'paciente.reprogramacion@demo.invalid', 'USER'
    UNION ALL SELECT 'paciente.recordatorio@demo.invalid', 'USER'
    UNION ALL SELECT 'paciente.inactivo@demo.invalid', 'USER'
) m
JOIN users u ON u.email = m.email
JOIN roles r ON r.code = m.code;

-- ------------------------------------------------------------ profesionales
INSERT INTO professionals (user_id, professional_code, license_number, active)
SELECT u.id, p.professional_code, p.license_number, p.active
FROM (
    SELECT 'dra.gomez@demo.invalid' email, 'PROF-DEMO-101' professional_code, 'RM-DEMO-0101' license_number, TRUE active
    UNION ALL SELECT 'dr.salazar@demo.invalid', 'PROF-DEMO-102', 'RM-DEMO-0102', TRUE
    UNION ALL SELECT 'dra.ortiz@demo.invalid', 'PROF-DEMO-103', 'RM-DEMO-0103', TRUE
    UNION ALL SELECT 'dr.inactivo@demo.invalid', 'PROF-DEMO-104', 'RM-DEMO-0104', FALSE
    UNION ALL SELECT 'multirol@demo.invalid', 'PROF-DEMO-105', 'RM-DEMO-0105', TRUE
) p
JOIN users u ON u.email = p.email
WHERE NOT EXISTS (SELECT 1 FROM professionals x WHERE x.professional_code = p.professional_code);

INSERT IGNORE INTO professional_specialties (professional_id, specialty_id, is_primary, active)
SELECT pr.id, sp.id, m.is_primary, TRUE
FROM (
    SELECT 'PROF-DEMO-101' pcode, 'MEDICINA_GENERAL' scode, TRUE is_primary
    UNION ALL SELECT 'PROF-DEMO-102', 'CARDIOLOGIA_ADULTO', TRUE
    UNION ALL SELECT 'PROF-DEMO-103', 'ORTOPEDIA_TRAUMATOLOGIA', TRUE
    UNION ALL SELECT 'PROF-DEMO-104', 'MEDICINA_GENERAL', TRUE
    UNION ALL SELECT 'PROF-DEMO-105', 'MEDICINA_GENERAL', TRUE
    UNION ALL SELECT 'PROF-DEMO-105', 'CARDIOLOGIA_ADULTO', FALSE
) m
JOIN professionals pr ON pr.professional_code = m.pcode
JOIN specialties sp ON sp.code = m.scode;

INSERT IGNORE INTO professional_locations (professional_id, location_id, active)
SELECT pr.id, l.id, TRUE
FROM (
    SELECT 'PROF-DEMO-101' pcode, 'HIC' lcode
    UNION ALL SELECT 'PROF-DEMO-101', 'ICV'
    UNION ALL SELECT 'PROF-DEMO-102', 'HIC'
    UNION ALL SELECT 'PROF-DEMO-103', 'ICV'
    UNION ALL SELECT 'PROF-DEMO-104', 'HIC'
    UNION ALL SELECT 'PROF-DEMO-105', 'HIC'
    UNION ALL SELECT 'PROF-DEMO-105', 'ICV'
) m
JOIN professionals pr ON pr.professional_code = m.pcode
JOIN locations l ON l.code = m.lcode;

-- ----------------------------------------------------------------- EPS demo
INSERT INTO eps (code, name, active)
SELECT s.code, s.name, s.active
FROM (
    SELECT 'EPS_DEMO_C' code, 'EPS Demo Magisterio' name, TRUE active
    UNION ALL SELECT 'EPS_DEMO_INACTIVA', 'EPS Demo Liquidada (inactiva)', FALSE
) s
WHERE NOT EXISTS (SELECT 1 FROM eps e WHERE e.code = s.code);

INSERT INTO eps_plans (eps_id, regime_id, code, name, active)
SELECT e.id, r.id, s.code, s.name, s.active
FROM (
    SELECT 'EPS_DEMO_C' ecode, 'CONTRIBUTIVO' rcode, 'C-CONTRIB' code, 'Plan Contributivo Magisterio Demo' name, TRUE active
    UNION ALL SELECT 'EPS_DEMO_C', 'EXCEPCION', 'C-EXCEP', 'Plan Excepción Magisterio Demo', TRUE
    UNION ALL SELECT 'EPS_DEMO_A', 'ESPECIAL', 'A-ESPECIAL-OFF', 'Plan Especial Demo (inactivo)', FALSE
    UNION ALL SELECT 'EPS_DEMO_INACTIVA', 'CONTRIBUTIVO', 'X-CONTRIB', 'Plan heredado Demo', FALSE
) s
JOIN eps e ON e.code = s.ecode
JOIN insurance_regimes r ON r.code = s.rcode
WHERE NOT EXISTS (SELECT 1 FROM eps_plans p WHERE p.eps_id = e.id AND p.code = s.code);

-- ------------------------------------------------------------------- citas
-- Cada fila define una cita; las "no terminales futuras" reciben su propio bloque y slots.
DROP TEMPORARY TABLE IF EXISTS seed_appt;
CREATE TEMPORARY TABLE seed_appt (
    patient_email VARCHAR(160) NOT NULL,
    prof_code VARCHAR(40) NOT NULL,
    loc_code VARCHAR(30) NOT NULL,
    spec_code VARCHAR(50) NOT NULL,
    status_code VARCHAR(40) NOT NULL,
    start_at DATETIME NOT NULL,
    minutes SMALLINT NOT NULL
);

INSERT INTO seed_appt (patient_email, prof_code, loc_code, spec_code, status_code, start_at, minutes) VALUES
 -- paciente con cita general aprobada (mañana) + una atendida en el pasado
 ('paciente.general@demo.invalid', 'PROF-DEMO-101', 'HIC', 'MEDICINA_GENERAL', 'APPROVED', TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00'), 30),
 ('paciente.general@demo.invalid', 'PROF-DEMO-101', 'HIC', 'MEDICINA_GENERAL', 'COMPLETED', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 7 DAY), '09:00:00'), 30),
 -- especialista: una solicitud pendiente de aprobación y una cita de 60 min aprobada
 ('paciente.especialista@demo.invalid', 'PROF-DEMO-102', 'HIC', 'CARDIOLOGIA_ADULTO', 'REQUESTED', TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00:00'), 30),
 ('paciente.especialista@demo.invalid', 'PROF-DEMO-103', 'ICV', 'ORTOPEDIA_TRAUMATOLOGIA', 'APPROVED', TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:00:00'), 60),
 -- historial con los cuatro estados terminales
 ('paciente.historial@demo.invalid', 'PROF-DEMO-102', 'HIC', 'CARDIOLOGIA_ADULTO', 'COMPLETED', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 14 DAY), '10:00:00'), 30),
 ('paciente.historial@demo.invalid', 'PROF-DEMO-101', 'ICV', 'MEDICINA_GENERAL', 'NO_SHOW', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 10 DAY), '11:00:00'), 30),
 ('paciente.historial@demo.invalid', 'PROF-DEMO-103', 'ICV', 'ORTOPEDIA_TRAUMATOLOGIA', 'CANCELLED', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '15:00:00'), 60),
 ('paciente.historial@demo.invalid', 'PROF-DEMO-102', 'HIC', 'CARDIOLOGIA_ADULTO', 'REJECTED', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '16:00:00'), 30),
 -- reprogramación: una solicitud PENDIENTE y otra RECHAZADA (el paciente debe decidir)
 ('paciente.reprogramacion@demo.invalid', 'PROF-DEMO-101', 'ICV', 'MEDICINA_GENERAL', 'APPROVED', TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:00:00'), 30),
 ('paciente.reprogramacion@demo.invalid', 'PROF-DEMO-101', 'HIC', 'MEDICINA_GENERAL', 'APPROVED', TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '16:00:00'), 30),
 -- recordatorio (WF-001): aprobada dentro de las próximas ~3 h, redondeada a 30 min
 ('paciente.recordatorio@demo.invalid', 'PROF-DEMO-105', 'HIC', 'MEDICINA_GENERAL', 'APPROVED',
    TIMESTAMP(DATE(NOW() + INTERVAL 3 HOUR), SEC_TO_TIME(CEIL(TIME_TO_SEC(TIME(NOW() + INTERVAL 3 HOUR)) / 1800) * 1800)), 30),
 -- paciente inactivo: WF-001 no debe recordarle aunque tenga cita aprobada
 ('paciente.inactivo@demo.invalid', 'PROF-DEMO-101', 'HIC', 'MEDICINA_GENERAL', 'APPROVED', TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00:00'), 30);

INSERT INTO appointments
    (patient_user_id, professional_id, location_id, specialty_id, status_id,
     scheduled_start_at, scheduled_end_at, created_by_user_id, approved_by_user_id, approved_at)
SELECT pu.id, pr.id, l.id, sp.id, st.id,
       s.start_at, DATE_ADD(s.start_at, INTERVAL s.minutes MINUTE), pu.id,
       CASE WHEN s.status_code IN ('APPROVED', 'COMPLETED', 'NO_SHOW', 'REJECTED') THEN adm.id END,
       CASE WHEN s.status_code IN ('APPROVED', 'COMPLETED', 'NO_SHOW', 'REJECTED')
            THEN LEAST(NOW(), DATE_SUB(s.start_at, INTERVAL 1 DAY)) END
FROM seed_appt s
JOIN users pu ON pu.email = s.patient_email
JOIN professionals pr ON pr.professional_code = s.prof_code
JOIN locations l ON l.code = s.loc_code
JOIN specialties sp ON sp.code = s.spec_code
JOIN appointment_statuses st ON st.code = s.status_code
JOIN users adm ON adm.email = 'admin@demo.invalid'
WHERE NOT EXISTS (
    SELECT 1 FROM appointments a
    WHERE a.patient_user_id = pu.id AND a.professional_id = pr.id AND a.scheduled_start_at = s.start_at
);

-- Historial de estados: una fila con el estado vigente de cada cita sembrada.
INSERT INTO appointment_status_history (appointment_id, status_id, changed_by_user_id, change_source, reason, changed_at)
SELECT a.id, a.status_id,
       CASE WHEN st.code IN ('APPROVED', 'REJECTED') THEN a.approved_by_user_id
            WHEN st.code IN ('COMPLETED', 'NO_SHOW') THEN NULL
            ELSE a.created_by_user_id END,
       CASE WHEN st.code IN ('APPROVED', 'REJECTED') THEN 'ADMIN'
            WHEN st.code IN ('COMPLETED', 'NO_SHOW') THEN 'SYSTEM'
            ELSE 'USER' END,
       CASE st.code WHEN 'REJECTED' THEN 'Sin disponibilidad del especialista (dato de laboratorio)'
                    WHEN 'CANCELLED' THEN 'Cancelada por el paciente (dato de laboratorio)' END,
       LEAST(NOW(), DATE_SUB(a.scheduled_start_at, INTERVAL 1 DAY))
FROM appointments a
JOIN appointment_statuses st ON st.id = a.status_id
JOIN seed_appt s ON s.start_at = a.scheduled_start_at
JOIN users pu ON pu.email = s.patient_email AND pu.id = a.patient_user_id
WHERE NOT EXISTS (SELECT 1 FROM appointment_status_history h WHERE h.appointment_id = a.id);

-- Bloques y slots propios de las citas futuras activas (REQUESTED/APPROVED).
INSERT INTO availability_blocks (professional_id, location_id, available_date, start_time, end_time, active)
SELECT DISTINCT a.professional_id, a.location_id, DATE(a.scheduled_start_at), TIME(a.scheduled_start_at), TIME(a.scheduled_end_at), TRUE
FROM appointments a
JOIN appointment_statuses st ON st.id = a.status_id AND st.code IN ('REQUESTED', 'APPROVED')
JOIN seed_appt s ON s.start_at = a.scheduled_start_at
JOIN users pu ON pu.email = s.patient_email AND pu.id = a.patient_user_id
WHERE a.scheduled_start_at > NOW()
  AND DATE(a.scheduled_start_at) = DATE(a.scheduled_end_at)
  AND NOT EXISTS (
      SELECT 1 FROM availability_blocks b
      WHERE b.professional_id = a.professional_id AND b.location_id = a.location_id
        AND b.available_date = DATE(a.scheduled_start_at)
        AND b.start_time = TIME(a.scheduled_start_at) AND b.end_time = TIME(a.scheduled_end_at)
  );

INSERT IGNORE INTO professional_slots (availability_block_id, start_at, end_at, appointment_id)
SELECT b.id,
       DATE_ADD(a.scheduled_start_at, INTERVAL n.n * 30 MINUTE),
       DATE_ADD(a.scheduled_start_at, INTERVAL (n.n + 1) * 30 MINUTE),
       a.id
FROM appointments a
JOIN appointment_statuses st ON st.id = a.status_id AND st.code IN ('REQUESTED', 'APPROVED')
JOIN seed_appt s ON s.start_at = a.scheduled_start_at
JOIN users pu ON pu.email = s.patient_email AND pu.id = a.patient_user_id
JOIN availability_blocks b ON b.professional_id = a.professional_id AND b.location_id = a.location_id
     AND b.available_date = DATE(a.scheduled_start_at)
     AND b.start_time = TIME(a.scheduled_start_at) AND b.end_time = TIME(a.scheduled_end_at)
JOIN (SELECT 0 n UNION ALL SELECT 1) n ON n.n * 30 < s.minutes
WHERE a.scheduled_start_at > NOW()
  AND DATE(a.scheduled_start_at) = DATE(a.scheduled_end_at);

-- ---------------------------------------------------------- reprogramaciones
-- PENDING: pide pasar la cita de +2d a +4d 15:00 en ICV; la franja pedida queda retenida.
INSERT INTO reschedule_requests
    (appointment_id, requested_by_user_id, requested_location_id, status_id,
     previous_start_at, previous_end_at, requested_start_at, requested_end_at)
SELECT a.id, a.patient_user_id, a.location_id, rs.id, a.scheduled_start_at, a.scheduled_end_at,
       TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:00:00'),
       TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:30:00')
FROM appointments a
JOIN users pu ON pu.id = a.patient_user_id AND pu.email = 'paciente.reprogramacion@demo.invalid'
JOIN reschedule_request_statuses rs ON rs.code = 'PENDING'
WHERE a.scheduled_start_at = TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:00:00')
  AND NOT EXISTS (SELECT 1 FROM reschedule_requests r WHERE r.appointment_id = a.id);

INSERT INTO availability_blocks (professional_id, location_id, available_date, start_time, end_time, active)
SELECT a.professional_id, a.location_id, DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:00:00', '15:30:00', TRUE
FROM appointments a
JOIN reschedule_requests r ON r.appointment_id = a.id
JOIN reschedule_request_statuses rs ON rs.id = r.status_id AND rs.code = 'PENDING'
JOIN users pu ON pu.id = a.patient_user_id AND pu.email = 'paciente.reprogramacion@demo.invalid'
WHERE r.requested_start_at = TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:00:00')
  AND NOT EXISTS (
      SELECT 1 FROM availability_blocks b
      WHERE b.professional_id = a.professional_id AND b.location_id = a.location_id
        AND b.available_date = DATE_ADD(CURDATE(), INTERVAL 4 DAY)
        AND b.start_time = '15:00:00' AND b.end_time = '15:30:00'
  );

INSERT IGNORE INTO professional_slots (availability_block_id, start_at, end_at, reschedule_request_id)
SELECT b.id, r.requested_start_at, r.requested_end_at, r.id
FROM reschedule_requests r
JOIN appointments a ON a.id = r.appointment_id
JOIN reschedule_request_statuses rs ON rs.id = r.status_id AND rs.code = 'PENDING'
JOIN users pu ON pu.id = a.patient_user_id AND pu.email = 'paciente.reprogramacion@demo.invalid'
JOIN availability_blocks b ON b.professional_id = a.professional_id AND b.location_id = a.location_id
     AND b.available_date = DATE(r.requested_start_at)
     AND b.start_time = TIME(r.requested_start_at) AND b.end_time = TIME(r.requested_end_at);

-- REJECTED: el ADMIN la rechazó y el paciente aún no decide (patient_action_after_rejection NULL).
INSERT INTO reschedule_requests
    (appointment_id, requested_by_user_id, requested_location_id, status_id,
     previous_start_at, previous_end_at, requested_start_at, requested_end_at,
     decision_reason, decided_by_user_id, decided_at)
SELECT a.id, a.patient_user_id, a.location_id, rs.id, a.scheduled_start_at, a.scheduled_end_at,
       TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '15:00:00'),
       TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 6 DAY), '15:30:00'),
       'La franja solicitada ya no está disponible (dato de laboratorio)', adm.id, NOW()
FROM appointments a
JOIN users pu ON pu.id = a.patient_user_id AND pu.email = 'paciente.reprogramacion@demo.invalid'
JOIN reschedule_request_statuses rs ON rs.code = 'REJECTED'
JOIN users adm ON adm.email = 'admin@demo.invalid'
WHERE a.scheduled_start_at = TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 5 DAY), '16:00:00')
  AND NOT EXISTS (SELECT 1 FROM reschedule_requests r WHERE r.appointment_id = a.id);

DROP TEMPORARY TABLE IF EXISTS seed_appt;
