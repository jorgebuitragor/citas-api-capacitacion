-- Datos sintéticos de disponibilidad para demostrar S3 sin credenciales operativas.
INSERT INTO users (first_name, last_name, document_type, document_number, email, phone, password_hash, active)
SELECT 'Profesional', 'General', 'CC', '900000001', 'prof.general@demo.invalid', '3000000001', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5v8Vg3oG5zv7R5pH5P5C1O6iO6KuzUe', FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'prof.general@demo.invalid');

INSERT INTO users (first_name, last_name, document_type, document_number, email, phone, password_hash, active)
SELECT 'Profesional', 'Especialista', 'CC', '900000002', 'prof.especialista@demo.invalid', '3000000002', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5v8Vg3oG5zv7R5pH5P5C1O6iO6KuzUe', FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'prof.especialista@demo.invalid');

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT id, 3 FROM users WHERE email IN ('prof.general@demo.invalid', 'prof.especialista@demo.invalid');

INSERT INTO professionals (user_id, professional_code, license_number, active)
SELECT id, 'PROF-GENERAL-001', 'RM-SINTETICO-001', TRUE FROM users WHERE email = 'prof.general@demo.invalid';

INSERT INTO professionals (user_id, professional_code, license_number, active)
SELECT id, 'PROF-ESPECIAL-001', 'RM-SINTETICO-002', TRUE FROM users WHERE email = 'prof.especialista@demo.invalid';

INSERT INTO professional_specialties (professional_id, specialty_id, is_primary, active)
SELECT p.id, 1, TRUE, TRUE FROM professionals p WHERE p.professional_code = 'PROF-GENERAL-001';

INSERT INTO professional_specialties (professional_id, specialty_id, is_primary, active)
SELECT p.id, 2, TRUE, TRUE FROM professionals p WHERE p.professional_code = 'PROF-ESPECIAL-001';

INSERT INTO professional_specialties (professional_id, specialty_id, is_primary, active)
SELECT p.id, 3, FALSE, TRUE FROM professionals p WHERE p.professional_code = 'PROF-ESPECIAL-001';

INSERT INTO professional_locations (professional_id, location_id, active)
SELECT p.id, 1, TRUE FROM professionals p WHERE p.professional_code IN ('PROF-GENERAL-001', 'PROF-ESPECIAL-001');

INSERT INTO availability_blocks (professional_id, location_id, available_date, start_time, end_time, active)
SELECT p.id, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00:00', '12:00:00', TRUE
FROM professionals p WHERE p.professional_code IN ('PROF-GENERAL-001', 'PROF-ESPECIAL-001');

INSERT INTO professional_slots (availability_block_id, start_at, end_at)
SELECT ab.id,
       TIMESTAMP(ab.available_date, ADDTIME(ab.start_time, SEC_TO_TIME(n.n * 1800))),
       TIMESTAMP(ab.available_date, ADDTIME(ab.start_time, SEC_TO_TIME((n.n + 1) * 1800)))
FROM availability_blocks ab
JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7) n
WHERE ab.available_date = DATE_ADD(CURDATE(), INTERVAL 2 DAY)
  AND ADDTIME(ab.start_time, SEC_TO_TIME((n.n + 1) * 1800)) <= ab.end_time;
