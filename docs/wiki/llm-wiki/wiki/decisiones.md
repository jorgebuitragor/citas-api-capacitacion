# Decisiones

## DEC-001 — Esquema de referencia como autoridad

- **Estado:** aprobada.
- **Fecha:** 2026-09-22.
- **Decisión:** el esquema inicial es `database/reference/db.sql`; MySQL lo ejecuta al crear el volumen y el backend se adapta a ese contrato de persistencia.
- **Consecuencia:** Flyway aplica baseline `4` al esquema inicializado por Docker, que ya contiene autenticación y agenda. Las migraciones futuras empiezan en `V5__...`; V1–V4 se conservan para evolucionar el volumen legado. La API mantiene IDs serializados como texto para conservar el contrato REST de autenticación actual.

## DEC-002 — Aprobación de la interfaz gráfica de autenticación

- **Estado:** aprobada.
- **Fecha:** 2026-09-22.
- **Decisión:** el usuario aprueba explícitamente la interfaz gráfica actual de `citas-web` para registro/login, tal como está implementada en `src/App.tsx`/`src/styles.css` y documentada en `citas-web/skills/web-design-governance/skills/web-design-governance/references/current-web-baseline.md`.
- **Consecuencia:** cierra la pregunta abierta de fuente visual pendiente en HU-002 y HU-003; esa línea base queda como referencia de diseño aprobada para futuras pantallas hasta que se reemplace por una fuente Stitch/AI Studio formal.

## DEC-003 — Exclusión de slots para reservas S3

- **Estado:** aprobada.
- **Fecha:** 2026-09-24.
- **Decisión:** la zona operativa es `America/Bogota`; una cita especializada `REQUESTED` retiene slots hasta la decisión ADMIN y no expira durante S3. La reserva bloquea los slots candidatos en orden ascendente mediante bloqueo pesimista, valida la franja completa y asigna todos los slots en una única transacción. Si no están todos libres y consecutivos, revierte y responde `409`.
- **Transiciones:** Medicina General crea `APPROVED` con fuente `SYSTEM` y sin actor; una especialidad crea `REQUESTED` con fuente `USER` y actor USER; ADMIN aprueba conservando slots o rechaza con motivo y los libera.
- **Consecuencia:** no hay reintento automático de servidor; el cliente recarga disponibilidad tras `409`. El contrato queda en `docs/contracts/appointments.md`.
