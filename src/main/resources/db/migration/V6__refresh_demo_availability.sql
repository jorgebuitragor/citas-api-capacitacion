-- Mantiene disponibilidad sintética futura cuando un volumen de laboratorio
-- permanece activo después de la fecha creada por una migración anterior.
INSERT INTO availability_blocks (professional_id, location_id, available_date, start_time, end_time, active)
SELECT p.id, pl.location_id, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', TRUE
FROM professionals p
JOIN professional_locations pl ON pl.professional_id = p.id AND pl.active = TRUE
WHERE p.professional_code IN ('PROF-GENERAL-001', 'PROF-ESPECIAL-001')
  AND p.active = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM availability_blocks existing
      WHERE existing.professional_id = p.id
        AND existing.location_id = pl.location_id
        AND existing.available_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY)
  );

INSERT INTO professional_slots (availability_block_id, start_at, end_at)
SELECT ab.id,
       TIMESTAMP(ab.available_date, ADDTIME(ab.start_time, SEC_TO_TIME(n.n * 1800))),
       TIMESTAMP(ab.available_date, ADDTIME(ab.start_time, SEC_TO_TIME((n.n + 1) * 1800)))
FROM availability_blocks ab
JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7) n
WHERE ab.available_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY)
  AND ab.active = TRUE
  AND ADDTIME(ab.start_time, SEC_TO_TIME((n.n + 1) * 1800)) <= ab.end_time
  AND NOT EXISTS (
      SELECT 1 FROM professional_slots existing
      WHERE existing.availability_block_id = ab.id
        AND existing.start_at = TIMESTAMP(ab.available_date, ADDTIME(ab.start_time, SEC_TO_TIME(n.n * 1800)))
  );
