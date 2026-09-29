# S4 — Checkpoint de preflight HU-020 / HU-021 (reprogramación)

- **Fecha:** 2026-09-29.
- **Resultado de este preflight:** **PAUSADO / ESCALADO** — no se inició implementación porque no se cumplían las condiciones de entrada declaradas en la meta.
- **Resultado final del objetivo:** **CUMPLIDO** tras las decisiones del usuario. Ver la sección «Reanudación, decisiones e implementación» al final del documento. Las tablas siguientes conservan el estado observado en el preflight y no se reescriben.
- **Repositorios revisados:** `citas-api` y `citas-web`, ambos en `develop`.
- **Seguridad de lectura:** no se abrieron `.env` ni credenciales.
- **Intentos de implementación consumidos:** 0 de 4 por criterio (no se inició iteración implementación/verificación).

## Fuentes leídas

- Raíz: `README.md`, `PRD.md` (RF-15 y RN-01, RN-05, RN-09, RN-10, RN-11), `RESTRICCIONES_TECNICAS.md`, `database/REQUISITOS_NORMALIZACION_3FN.md`, `AGENTS.md`.
- Repositorios: `citas-api/README.md`, `citas-api/AGENTS.md`, `citas-web/README.md`, `citas-web/AGENTS.md`.
- Especificación: HU-014, HU-015, HU-016, HU-017, HU-018, HU-019, HU-020, HU-021 y `docs/wiki/scrum/README.md`.
- Contrato: `docs/contracts/appointments.md` (S3 y sección aprobada HU-018/HU-019).
- LLM Wiki: `wiki/index.md`, `decisiones.md` (DEC-001, DEC-002, DEC-003), `riesgos-y-preguntas-abiertas.md`, `trazabilidad-hu.md`.
- Evidencia previa: `docs/evidence/S3-booking-red-green.md`, `docs/evidence/goals-loops/S4/HU-018-HU-019-preflight-checkpoint.md`.
- Esquema: `src/main/resources/db/migration/V1..V6` y `database/reference/db.sql` (lectura comparativa autorizada por DEC-001).
- Diseño disponible: `citas-web/docs/design/HU-018-HU-019-mis-citas-proposal.md`.

## Verificación de condiciones de entrada

| Condición exigida por la meta | Evidencia observada | Estado |
|---|---|---|
| HU-018 completada con evidencia | `estado: Completada`; CA-01..CA-03 y DoD `Cumple` con enlaces a `BookingIntegrationTest.userListsOnlyOwnAppointmentsAndCanFilterByStatusAndDate`, `src/components/MyAppointments.tsx` y contrato. | **Satisfecha** |
| HU-014 completada con evidencia | `estado: Pendiente de aprobación`; tabla de evidencia con CA-01, CA-02, CA-03 y DoD en `Pendiente`, sin enlaces. Nota de la HU: «Definir concurrencia y vida de retención antes de aprobar implementación». El endpoint `GET /api/v1/availability` existe en el contrato S3 y en la implementación, pero la HU no está aprobada ni cerrada. | **NO satisfecha — bloquea inicio** |
| Flujos de reserva requeridos completados con evidencia | HU-015, HU-016 y HU-017 `Aprobada`, con CA/DoD marcados `Cumplido`, pruebas `BookingIntegrationTest` nombradas, contrato y evidencia S3; cierre registrado el 2026-09-29 en `trazabilidad-hu.md`. | **Satisfecha** |
| Regla de motivo para rechazo de reprogramación aprobada | No existe decisión aprobada. `decisiones.md` sólo contiene DEC-001, DEC-002 y DEC-003; ninguna cubre reprogramación. HU-021 registra en sus notas: «"Motivo cuando corresponda" requiere definir obligatoriedad exacta para rechazo de reprogramación». | **NO satisfecha — bloquea inicio** |

## Bloqueos adicionales detectados en el preflight

| Requisito | Evidencia observada | Estado |
|---|---|---|
| HU-020 aprobada | `estado: Pendiente de aprobación`; CA-01..CA-03 y DoD `Pendiente`. Nota de la HU: «Candidata a subdivisión tras acordar modelo/retención; **no se implementa hasta resolverlo**». | Bloquea inicio |
| HU-021 aprobada | `estado: Pendiente de aprobación`; CA-01..CA-03 y DoD `Pendiente`. Su DoD exige decidir la subdivisión si no se elimina el riesgo «Muy alto» con un diseño aprobado. | Bloquea inicio |
| Contrato REST de reprogramación | `docs/contracts/appointments.md` no define ninguna operación de reprogramación: no hay ruta de creación de solicitud, consulta de pendientes, decisión ADMIN, proyección, códigos de error ni autorización. `contratos-rest.md` tampoco. | Falta |
| Diseño aprobado de las pantallas | Las pantallas obligatorias «solicitar reprogramación» y «aprobar/rechazar reprogramaciones» (PRD §6) no tienen diseño aprobado. `HU-018-HU-019-mis-citas-proposal.md` excluye explícitamente reprogramación y DEC-002 aprueba sólo autenticación. El prototipo `medischedule` se acotó en el checkpoint anterior y su reprogramación quedó fuera de alcance. | Falta |
| Decisión de subdivisión de HU-020/HU-021 | Ambas HU son esfuerzo «Muy alto» y su DoD obliga a reconsiderar la división antes de cerrar. No hay decisión registrada. | Falta |

## Hallazgo de esquema (informativo, no requiere decisión nueva)

- `database/reference/db.sql` ya modela la reprogramación en 3FN: catálogo `reschedule_request_statuses` (seed `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`) y tabla `reschedule_requests` con `appointment_id`, `requested_by_user_id`, `requested_location_id`, `status_id`, `previous_start_at`/`previous_end_at`, `requested_start_at`/`requested_end_at`, `decision_reason`, `decided_by_user_id`, `decided_at` y `patient_action_after_rejection` restringido a `KEEP_APPOINTMENT`/`CANCEL_APPOINTMENT`.
- La línea Flyway activa (`V1`..`V6`) **no** crea esas tablas: V3 sólo añade `locations`, `specialties`, `professionals`, sus puentes, `appointment_statuses`, `appointments`, `availability_blocks`, `professional_slots` y `appointment_status_history`.
- Consecuencia: al implementar se requerirá una migración nueva `V7__...` idempotente que materialice `reschedule_request_statuses` y `reschedule_requests` con la forma de referencia, coherente con DEC-001. No se edita ninguna migración aplicada. El punto se deriva de DEC-001 y no constituye una regla abierta.

## Reglas del PRD que la implementación deberá preservar (sin inventar valores)

- RF-15 y RN-10: la cita original conserva su franja hasta que ADMIN decida; la nueva franja se retiene mientras la solicitud está `PENDING`.
- RF-15: la reprogramación conserva profesional y especialidad; cambiar profesional se trata como nueva cita y queda fuera de alcance.
- RN-01 y RN-05: la nueva franja debe estar libre y completa; 60 minutos exige dos slots consecutivos. Sin franja completa no se crea solicitud ni retención parcial.
- RN-09: aprobar libera los slots originales; rechazar libera la retención nueva.
- RN-11 y RF-19: cada transición se registra explícitamente con cita, estado nuevo, actor, fuente, fecha/hora y motivo aplicable.
- Fuera de alcance explícito de la meta: cancelación automática de la original tras rechazo, cambio de profesional y cualquier valor para reglas abiertas.

## Decisiones solicitadas para reanudar

1. **Regla de motivo para el rechazo de reprogramación** (condición de entrada de la meta y regla abierta de HU-021): definir si `reason` es obligatorio en todo rechazo ADMIN o sólo en casos determinados, y con qué validación/límites. No se propone valor por inferencia.
2. **Aprobar y acreditar HU-014** con CA-01..CA-03 y DoD cerrados y evidencia enlazada (la disponibilidad ya existe en el contrato S3 y en las pruebas de reserva, pero la HU no está cerrada).
3. **Aprobar HU-020 y HU-021** con sus criterios y DoD, y resolver si se subdividen o se mantienen íntegras.
4. **Aprobar/documentar el contrato REST de reprogramación**: creación de solicitud por USER, consulta de pendientes por ADMIN, decisión ADMIN, proyección de estado y motivo, autorización y códigos de error. Actualmente no existe.
5. **Aportar/aprobar el diseño visual** de «solicitar reprogramación» (USER) y «aprobar/rechazar reprogramaciones» (ADMIN), con estados `loading`, `empty`, `error`, `success` y `disabled`.

## Alcance preparado para el siguiente intento

Una vez satisfechas las condiciones, el cambio será cross-repo y quedará limitado a HU-020/HU-021:

- **`citas-api`:** migración `V7` con `reschedule_request_statuses` y `reschedule_requests` según la referencia; puertos y caso de uso de solicitud (elegibilidad de cita propia `APPROVED` y futura, mismo profesional y especialidad, bloqueo pesimista ascendente y retención atómica de la franja completa sin liberar la original); caso de uso de decisión ADMIN (aprobación que libera los slots originales y asigna los nuevos en una sola transacción; rechazo que libera la retención nueva y conserva la cita original); eventos en `appointment_status_history` con actor, fuente y motivo aplicable; autorización por rol y ownership; extensión del contrato; pruebas de integración para elegibilidad, actor no autorizado, cambio de profesional, franja conflictiva o incompleta, atomicidad de ambas decisiones y auditoría.
- **`citas-web`:** flujo de solicitud sobre la vista de citas existente y bandeja de decisión ADMIN, ajustados al diseño aprobado; estados de UI, motivo aplicable, prevención de doble envío y mensajes de error del contrato; pruebas Vitest, `typecheck` y `build`.
- **Verificación prevista:** `mvn test` en el contenedor API, `npm run typecheck && npm run test && npm run build` en web, y revisión productor-consumidor del contrato.
- **No se incluirá:** cancelación automática tras rechazo, cambio de profesional, expiración de retenciones ni valores para reglas abiertas.

## Resultado

- No se modificó código, esquema, contrato, historias ni diseño. Este archivo es el checkpoint de evidencia solicitado por la meta.
- Los cambios locales preexistentes de HU-018/HU-019 en ambos repositorios se inspeccionaron y se conservan intactos.
- Estado final de este intento: **PAUSADO POR PRECONDICIONES Y DECISIÓN ABIERTA**, escalado al usuario.

---

# Reanudación, decisiones e implementación — 2026-09-29

## Decisiones aportadas por el usuario

Tras la escalada, el usuario resolvió las cuatro condiciones abiertas. Quedan registradas en DEC-005 de `docs/wiki/llm-wiki/wiki/decisiones.md`:

1. **Motivo del rechazo:** obligatorio y no vacío para `REJECT`; opcional en `APPROVE` y auditado si se envía.
2. **Aprobaciones:** HU-014, HU-020 y HU-021 aprobadas con sus criterios y DoD actuales.
3. **Contrato y diseño:** redactados como propuesta por el agente y aprobados explícitamente por el usuario antes de tocar código.
4. **Subdivisión:** HU-020 y HU-021 se mantienen íntegras en un único incremento cross-repo.

Decisiones adicionales tomadas por el usuario al aprobar la propuesta de contrato:

- **Retención de la nueva franja:** columna nueva `professional_slots.reschedule_request_id`, elegida frente a reutilizar `appointment_id` para ambas franjas. Es una extensión deliberada sobre `database/reference/db.sql`.
- **Sede:** la reprogramación puede cambiar de sede entre las habilitadas para el mismo profesional y especialidad, porque RF-15 solo fija profesional y especialidad.

## Cambios en `citas-api`

- `src/main/resources/db/migration/V7__add_reschedule_requests.sql`: crea `reschedule_request_statuses` con su seed, `reschedule_requests` según el esquema de referencia y la columna `professional_slots.reschedule_request_id` con su clave foránea. Idempotente mediante `CREATE TABLE IF NOT EXISTS`, `ON DUPLICATE KEY UPDATE` y comprobaciones sobre `information_schema`, porque los volúmenes creados desde `database/reference/db.sql` ya contienen las dos tablas.
- `src/main/java/co/fcv/citas/domain/appointment/RescheduleStatus.java`: nuevo enum de dominio.
- `BookingPorts.java`: `Slot` incorpora `rescheduleRequestId` y el predicado `free()`; nuevos registros `NewRescheduleRequest`, `RescheduleRequest` y `RescheduleRequestDetail`; nuevas operaciones de puerto para crear, retener, liberar, confirmar, mover franja, consultar y decidir.
- `BookingCommands.java`: `RequestReschedule` y `RescheduleDecision`.
- `BookingService.java`: casos de uso `requestReschedule`, `rescheduleRequests` y `decideReschedule`; la disponibilidad y la creación de citas pasan a usar `Slot.free()`, de modo que una franja retenida no se ofrece ni se puede reservar.
- `BookingPersistenceAdapter.java`: implementación JDBC con bloqueo pesimista; `lockRescheduleRequest` bloquea solicitud y cita en la misma consulta para serializar con una cancelación concurrente.
- `BookingController.java`: `POST /api/v1/appointments/{id}/reschedule-requests`, `GET /api/v1/admin/reschedule-requests` y `POST /api/v1/admin/reschedule-requests/{id}/decision`; el objeto de cita añade `rescheduleAllowed` y `rescheduleRequest`.
- `BookingIntegrationTest.java`: seis pruebas nuevas de integración.
- `AuthIntegrationTest.java`: la prueba CORS fija `app.cors.allowed-origins` en su `@SpringBootTest`. Corrección fuera del alcance funcional, explicada más abajo.

No se modificó `SecurityConfiguration`: `/api/v1/admin/**` ya exige ADMIN y `/api/v1/appointments/**` ya exige USER.

## Cambios en `citas-web`

- `src/api/booking.ts`: tipos `RescheduleStatus`, `RescheduleSummary` y `RescheduleRequest`; `MyAppointment` añade `rescheduleAllowed` y `rescheduleRequest`; clientes `requestReschedule`, `rescheduleRequests` y `decideReschedule`.
- `src/components/MyAppointments.tsx`: acción «Reprogramar» condicionada a `rescheduleAllowed`, bloques «Reprogramación pendiente», «Reprogramación rechazada» con su motivo y «Reprogramación aprobada», y el diálogo `RescheduleDialog` con selección de sede habilitada, fecha y franja disponible.
- `src/App.tsx`: la bandeja ADMIN se divide en dos secciones y añade `RescheduleInbox`, que compara franja actual y solicitada y exige motivo para rechazar.
- `src/styles.css`: estilos de los bloques, el diálogo y la comparación de franjas, con reglas responsive a 620 px.
- `src/components/MyAppointments.test.tsx` y `src/RescheduleInbox.test.tsx`: nueve pruebas nuevas.

## Verificación ejecutada

| Comprobación | Comando | Resultado |
|---|---|---|
| Pruebas backend | `docker compose exec -T citas-api-dev mvn -o test` | **BUILD SUCCESS** — 25 pruebas, 0 fallos, 0 errores (`AuthIntegrationTest` 5, `BookingIntegrationTest` 20). |
| Migración aplicada | consulta a `flyway_schema_history` | versiones 1 a 7 con `success = 1`; `professional_slots.reschedule_request_id` existe; catálogo con `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`. |
| Typecheck frontend | `npm run typecheck` en `citas-web-dev` | correcto, sin errores. |
| Pruebas frontend | `npm run test` en `citas-web-dev` | 4 archivos, **23 pruebas correctas**. |
| Build frontend | `npm run build` en `citas-web-dev` | correcto; 34 módulos transformados. |

### Cobertura de criterios

| Criterio | Prueba |
|---|---|
| HU-020 CA-01 | `userRequestsReschedulePendingHoldsNewSlotAndKeepsOriginal` |
| HU-020 CA-02 | `userRequestsReschedulePendingHoldsNewSlotAndKeepsOriginal`; `MyAppointments.test.tsx` «shows the pending reschedule without losing the original slot» |
| HU-020 CA-03 | `rescheduleRequestRejectsForeignProfessionalChangeConflictAndIneligibleAppointment` |
| HU-021 CA-01 | `adminApprovalSwapsSlotsAtomicallyAndKeepsAudit` |
| HU-021 CA-02 | `adminRejectionRequiresReasonReleasesHoldAndKeepsOriginalAppointment` |
| HU-021 CA-03 | ambas pruebas de decisión, con aserciones sobre `appointment_status_history` |
| HU-014 CA-03 | `heldRescheduleSlotIsNotOfferedNorReservable` |
| Autorización | `rescheduleInboxAndDecisionRequireAdminRoleAndRequestRequiresUserRole` |
| Frontend, estados y errores | nueve pruebas en `MyAppointments.test.tsx` y `RescheduleInbox.test.tsx` |

## Hallazgos durante la verificación

1. **Falla CORS ajena al alcance.** `AuthIntegrationTest.corsAllowsOnlyConfiguredOriginWithCredentials` fallaba con `403 Invalid CORS request`. Causa: `docker-compose.yml` define `APP_CORS_ALLOWED_ORIGINS=http://localhost:5173` y una variable de entorno tiene más precedencia en Spring Boot que `src/test/resources/application.yml`, que fija `http://localhost:3000`. La prueba dependía del host. Se corrigió fijando la propiedad en su propia anotación `@SpringBootTest`, sin tocar la configuración de producción ni el comportamiento de CORS.
2. **Residuos en el MySQL compartido.** La aserción de conteo exacto de `professionalSeesOnlyOwnApprovedAgendaFilteredByDayWeekAndLocation` (HU-022) fallaba por citas sintéticas dejadas por ejecuciones de pruebas de otra sesión. Se eliminaron los registros residuales y la suite quedó verde. Esa aserción de conteo exacto sigue siendo frágil frente a datos residuales.
3. **Edición y ejecución concurrentes.** Otra sesión implementaba HU-022/HU-023 sobre los mismos archivos Java y contra el mismo MySQL. Se produjeron una colisión de identificadores de decisión, resuelta renumerando la propia a `DEC-005`, y varias relecturas obligadas antes de escribir. Recomendación operativa: no paralelizar HU que comparten archivos y base de datos.

## Fuera de alcance, no implementado

Cancelación automática de la cita tras un rechazo, cambio de profesional, elección posterior del paciente (`patient_action_after_rejection` queda `NULL`), expiración de retenciones y transición `CANCELLED` de la solicitud. Ninguna regla abierta recibió un valor inventado.

## Estado final

**OBJETIVO CUMPLIDO.** HU-014, HU-020 y HU-021 quedan `Completada` con CA y DoD enlazados a símbolos y pruebas concretas. Contrato productor-consumidor verificado con pruebas de integración backend, pruebas de componente frontend, typecheck y build. No se hizo commit.
