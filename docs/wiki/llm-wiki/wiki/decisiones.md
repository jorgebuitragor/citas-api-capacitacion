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
- **Consecuencia:** cierra la pregunta abierta registrada en las notas de HU-025 ("Matriz exacta de lectura por rol pendiente de contrato") y en `riesgos-y-preguntas-abiertas.md`, y habilita el inicio de GOAL S4.04. CA-03 de HU-025 ("Visibilidad controlada") se implementa reforzando ownership en el caso de uso o adaptador autorizado (no solo ocultando controles en la UI), tal como ya anota el contrato.
- **Actualización 2026-10-02:** el usuario instruyó explícitamente marcar HU-024 y HU-025 como `Completada` en `docs/wiki/scrum/`. Se actualizó directamente (campo `estado`, tareas, DoD, tabla de evidencia e historial de validación en cada HU; lista de "HU aprobadas" y estado de EP-008 en el README del backlog), como excepción puntual a la regla de `AGENTS.md` de que solo `scrum-spec-orchestrator` escribe en esa carpeta. La excepción queda registrada aquí para trazabilidad; no cambia la regla general.
- **Actualización 2026-10-02 (continuación):** el mismo análisis detectó que HU-022 y HU-023 (GOAL S4.03) tenían el mismo defecto desde el 2026-09-29 — implementadas, probadas y con evidencia en `docs/evidence/goals-loops/S4/HU-022-HU-023-implementation.md`, pero con `estado: Pendiente de aprobación` en sus archivos. El usuario instruyó corregirlas con la misma excepción puntual; se aplicó el mismo tratamiento (campo `estado`, tareas, DoD, tabla de evidencia e historial en cada HU; lista de "HU aprobadas" y estado de EP-007 en el README del backlog).

## DEC-008 — Aprobación de HU-004 (recuperación de contraseña): duración, canal y contrato

- **Estado:** aprobada.
- **Fecha:** 2026-10-02.
- **Decisión:** el token de recuperación expira a los **15 minutos**, es de un solo uso y se persiste únicamente como hash SHA-256 (mismo patrón que `refresh_tokens`). El canal de entrega en desarrollo, autorizado por RF-03 del PRD ("en desarrollo puede exponerse de forma segura en log/respuesta controlada"), es **log del servidor** — el token en texto plano nunca viaja en la respuesta HTTP, se escribe en el log de `citas-api` con marca `[DEV]`.
- **Contrato aprobado:** `POST /api/v1/auth/password-reset/request` con `{ "email" }` responde `202 Accepted` siempre, exista o no el email, para no revelar qué cuentas están registradas (si el email corresponde a un usuario activo, crea el token y lo loguea; si no, no hace nada y responde igual). `POST /api/v1/auth/password-reset/confirm` con `{ "token", "newPassword" }` responde `200` si el token es válido/vigente/no consumido (hashea la nueva contraseña con BCrypt y consume el token) o `401` genérico ("Authentication failed", el mismo mensaje que login/refresh) para token inválido, vencido o ya usado, sin distinguir el motivo.
- **Consecuencia:** cierra la pregunta abierta registrada en las notas de HU-004 ("Duración y canal exactos son pregunta abierta") y habilita el inicio de GOAL S4.05. No se revoca la sesión activa del usuario ni se envían notificaciones adicionales al completar el cambio — queda fuera del alcance aprobado para esta HU.

## DEC-009 — Aprobación de HU-006/HU-007/HU-008 (catálogos configurables): esquema y contrato

- **Estado:** aprobada.
- **Fecha:** 2026-10-03.
- **Unicidad resuelta sin pregunta al usuario:** `database/reference/db.sql` (autoridad fijada en DEC-001) ya define `eps.code UNIQUE` (no el nombre) y `eps_plans` único por `(eps_id, code)` — la nota abierta de HU-006 ("unicidad nominal/código se define en contrato/modelo") se resuelve siguiendo esa fuente, sin inventar una regla nueva.
- **Decisión:** se traen a Flyway, tal cual el esquema de referencia, las tablas `insurance_regimes` (catálogo fijo, seed de 5 regímenes), `eps` y `eps_plans`. `specialties` ya existe desde V3 y no se modifica su estructura.
- **Contrato aprobado:** CRUD de EPS y planes bajo `/api/v1/admin/catalogs/eps` (ADMIN); gestión de especialidades (crear/editar) bajo `/api/v1/admin/catalogs/specialties` (ADMIN), reutilizando la tabla `specialties` ya sembrada; catálogo fijo de regímenes expuesto en `GET /api/v1/catalogs/insurance-regimes` (mismo rol `USER` que `/catalogs/locations` y `/catalogs/specialties`). Ninguna operación borra físicamente: solo `active` se alterna (RF-06).
- **Inmutabilidad deliberada:** `code` (EPS, plan, especialidad) y `durationMinutes`/`general`/`requiresAdminApproval` (especialidad) no se pueden editar después de creados — cambiar la duración de una especialidad retroactivamente corrompería la cantidad de slots ya reservados para citas existentes. Solo `name` y `active` son editables. No se pidió esta restricción explícitamente en las HU; es una decisión de ingeniería para preservar integridad de datos ya persistidos, documentada aquí para que quede trazable.
- **Consecuencia:** cierra la pregunta abierta de EP-002 ("La semántica exacta de unicidad/nombre de cada catálogo requiere contrato y modelo propio") y habilita el inicio de GOAL S4.06. La nota de HU-007 sobre alinear "plan activo" con la edición de afiliación queda fuera de alcance: HU-005 (editar afiliación) no es parte de este incremento.
- **Actualización 2026-10-03:** el usuario instruyó marcar HU-004, HU-006, HU-007 y HU-008 como `Completada` en `docs/wiki/scrum/` (misma excepción puntual a `AGENTS.md` que en DEC-007). HU-007 CA-02 se dejó `Parcial` porque depende de HU-005 (afiliación), aún sin implementar.

## DEC-010 — Aprobación de HU-028 (recordatorios, WF-001): reglas y fuente REST

- **Estado:** aprobada.
- **Fecha:** 2026-10-04.
- **Decisión:** el usuario aprobó, para el flujo S5.01: **ventana** = citas `APPROVED` que empiezan en las próximas 24 h (configurable por parámetro, 1–72); **destinatario** = el email sintético del paciente; **deduplicación** = un recordatorio por cita y por franja (si la cita se reprograma, la nueva franja vuelve a ser elegible), registrada en la tabla `appointment_reminders`; **fuente REST** = nuevo endpoint de solo lectura `GET /api/v1/automation/upcoming-appointments`, autenticado con una API key de servicio (`X-Api-Key`, variable de entorno `AUTOMATION_API_KEY`), sin JWT de usuario, más `POST /api/v1/automation/appointments/{id}/reminders` para registrar el resultado. Contrato en `docs/contracts/automation.md`.
- **Entorno aprobado:** n8n del trainer (el usuario lo configura; yo no manejo sus credenciales); entrega de la prueba controlada con **Mailpit local** y nodo Gmail incluido en el JSON sin credenciales; S5.02 con un escenario simulado.
- **Consecuencia:** habilita el inicio de GOAL S5.01 en lo que no depende de n8n (endpoint, tabla, pruebas, JSON). La ejecución en n8n y la verificación MCP quedan pausadas hasta que el usuario aporte acceso. Este endpoint es el único cambio en `citas-api` y no altera el núcleo de reservas. La actualización formal de HU-028 en `docs/wiki/scrum/` sigue pendiente.
