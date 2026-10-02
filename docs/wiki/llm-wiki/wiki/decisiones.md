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

## DEC-006 — Aprobación de HU-022/HU-023 y criterio de "pasada/aplicable"

- **Estado:** aprobada.
- **Fecha:** 2026-09-29.
- **Decisión:** el usuario aprobó explícitamente, en el chat del goal S4, el inicio de implementación de HU-022 (Consultar agenda profesional) y HU-023 (Cerrar atención). Para RF-17, una cita se considera "pasada/aplicable" cuando `endAt <= ahora` en `America/Bogota` (la cita ya terminó por completo), sin ventana de tolerancia adicional y con el mismo criterio para citas de 30 y 60 minutos.
- **Consecuencia:** cierra la pregunta abierta registrada en `riesgos-y-preguntas-abiertas.md`, en las notas de HU-023 y en "Riesgos e incógnitas" de EP-007. Habilita el contrato REST y la implementación de cierre de atención con elegibilidad `status = APPROVED` y `endAt <= now`. La actualización formal del campo `estado` en `docs/wiki/scrum/historias-de-usuario/HU-022-*.md` y `HU-023-*.md` queda pendiente de una ejecución de `scrum-spec-orchestrator` (única responsable de escribir en `docs/wiki/scrum/`); esta decisión documenta la aprobación de negocio que habilita avanzar mientras tanto.

## DEC-005 — Motivo y alcance de la decisión de reprogramación

- **Estado:** aprobada.
- **Fecha:** 2026-09-29.
- **Decisión:** el rechazo ADMIN de una solicitud de reprogramación exige `reason` no vacío; sin motivo la decisión se rechaza y no produce ningún cambio. La aprobación admite un `reason` **opcional** que, si se envía, se conserva en la auditoría. Esto cierra la regla abierta registrada en las notas de HU-021 (motivo «cuando corresponda»).
- **Alcance aprobado en la misma decisión:** HU-014, HU-020 y HU-021 quedan aprobadas con sus criterios y DoD actuales; HU-020 y HU-021 **no se subdividen** y se implementan como un único incremento cross-repo, dejando registrado en su DoD que la subdivisión se evaluó y se descartó.
- **Retención aprobada:** la nueva franja se retiene con una columna nueva `professional_slots.reschedule_request_id`, dejando `appointment_id` intacto en la franja original. Un slot está libre solo cuando ambas columnas son `NULL`. Es una extensión deliberada sobre `database/reference/db.sql`, elegida explícitamente por el usuario frente a la alternativa de reutilizar `appointment_id` para ambas franjas, porque hace la retención provisional distinguible de una reserva firme.
- **Sede aprobada:** la reprogramación puede cambiar de sede entre las habilitadas para el mismo profesional y especialidad, porque RF-15 solo fija profesional y especialidad y el esquema de referencia guarda `requested_location_id`.
- **Consecuencia:** la regla de motivo es consistente con RN-04 y con la decisión de cita especializada ya implementada en HU-017, donde el rechazo sin motivo responde `400`. El contrato REST de reprogramación quedó aprobado el 2026-09-29 en `docs/contracts/appointments.md`. No se aprueban cancelación automática tras rechazo, cambio de profesional ni expiración de retenciones.

## DEC-007 — Aprobación de HU-024/HU-025 y matriz de visibilidad de auditoría

- **Estado:** aprobada.
- **Fecha:** 2026-10-02.
- **Decisión:** el usuario aprueba HU-024 (Consultar bandeja administrativa) sin cambios sobre su alcance y CA actuales. Para HU-025 (Consultar auditoría de estados), la matriz de visibilidad por rol es la ya borrador en `docs/contracts/appointments.md:398` — por **ownership**: ADMIN consulta el historial de auditoría de cualquier cita; USER consulta el de sus propias citas; PROFESSIONAL consulta el de las citas que atendió. No se adopta la alternativa de restringir el endpoint únicamente a ADMIN.
- **Consecuencia:** cierra la pregunta abierta registrada en las notas de HU-025 ("Matriz exacta de lectura por rol pendiente de contrato") y en `riesgos-y-preguntas-abiertas.md`, y habilita el inicio de GOAL S4.04. CA-03 de HU-025 ("Visibilidad controlada") se implementa reforzando ownership en el caso de uso o adaptador autorizado (no solo ocultando controles en la UI), tal como ya anota el contrato. La actualización formal del campo `estado` en `docs/wiki/scrum/historias-de-usuario/HU-024-*.md` y `HU-025-*.md` queda pendiente de una ejecución de `scrum-spec-orchestrator` (única responsable de escribir en `docs/wiki/scrum/`); esta decisión documenta la aprobación de negocio que habilita avanzar mientras tanto.
