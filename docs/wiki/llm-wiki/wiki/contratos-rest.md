# Contratos REST

Estado: autenticación y reservas S3 documentadas; HU-024/HU-025 tienen una propuesta pendiente de aprobación. El detalle está en `../../contracts/appointments.md`.

Secciones documentadas en ese archivo:

- **S3 — disponibilidad y reservas:** catálogos, `GET /api/v1/availability`, creación de cita y decisión ADMIN de cita especializada.
- **HU-018 / HU-019 — citas propias:** `GET /api/v1/appointments` con filtros `status` y `date`, y `POST /api/v1/appointments/{id}/cancellation`.
- **HU-020 / HU-021 — reprogramación (aprobado el 2026-09-29):** `POST /api/v1/appointments/{id}/reschedule-requests`, `GET /api/v1/admin/reschedule-requests` y `POST /api/v1/admin/reschedule-requests/{id}/decision`. El objeto de cita se extiende de forma aditiva con `rescheduleAllowed` y `rescheduleRequest`. La regla de motivo está en DEC-005 y la retención provisional usa `professional_slots.reschedule_request_id`.
- **HU-024 / HU-025 — propuesta pendiente:** se documentaron filtros de bandeja para `REQUESTED` y `PENDING`, y `GET /api/v1/appointments/{appointmentId}/status-history` como consulta de solo lectura. La matriz propuesta es ADMIN cualquier cita, USER sus propias citas y PROFESSIONAL sus citas; requiere aprobación explícita antes de implementar.
