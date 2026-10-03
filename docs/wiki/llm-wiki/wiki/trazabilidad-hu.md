# Trazabilidad de historias de usuario

## HU-006 — Gestionar EPS

- **HECHO:** 2026-10-03 — HU aprobada vía DEC-009. ADMIN crea/consulta/actualiza EPS; `code` único e inmutable (`database/reference/db.sql`, DEC-001); sin borrado físico, solo `active`.
- **EVIDENCIA:** `CatalogIntegrationTest.createsEpsListsItIncludingInactiveAndRejectsDuplicateCode`, `epsManagementRequiresAdminRole`; contrato en `docs/contracts/catalogs.md`; evidencia completa en `docs/evidence/goals-loops/S4/HU-006-HU-007-HU-008-implementation.md`.
- **VERIFICACIÓN:** API con 41 pruebas en verde; frontend con typecheck, 39 pruebas Vitest y build correctos; verificación manual en navegador real (crear/desactivar EPS contra backend y MySQL reales).
- **PREGUNTA ABIERTA:** actualización formal del campo `estado` de HU-006 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`.

## HU-007 — Gestionar planes de EPS

- **HECHO:** 2026-10-03 — HU aprobada vía DEC-009. ADMIN crea/consulta/actualiza planes bajo una EPS válida; `code` único por EPS (no global); régimen inexistente responde `400`, EPS inexistente responde `404`.
- **EVIDENCIA:** `CatalogIntegrationTest.createsPlanUnderExistingEpsAndValidatesEpsAndRegimeReferences`, `plansManagementRequiresAdminRole`; contrato en `docs/contracts/catalogs.md`; evidencia completa en `docs/evidence/goals-loops/S4/HU-006-HU-007-HU-008-implementation.md`.
- **VERIFICACIÓN:** verificación manual creó un plan real bajo "Atención Particular Demo" y lo desactivó contra backend y MySQL reales.
- **RIESGO DETECTADO Y RESUELTO:** el preflight CORS rechazaba `PATCH` con `403` (`SecurityConfiguration` solo permitía `GET, POST, OPTIONS`; ningún endpoint anterior había usado `PATCH`). Corregido agregando `PATCH` a `allowedMethods`, con prueba de regresión `corsAllowsPatchForConfiguredOrigin` — las pruebas con `MockMvc` sin encabezado `Origin` no disparan el filtro CORS, por eso ninguna prueba previa lo detectó.
- **PREGUNTA ABIERTA:** actualización formal del campo `estado` de HU-007 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`.

## HU-008 — Gestionar especialidades

- **HECHO:** 2026-10-03 — HU aprobada vía DEC-009. ADMIN crea/actualiza especialidades con duración exclusivamente 30 o 60 minutos; `code`/`durationMinutes`/`general`/`requiresAdminApproval` inmutables tras crear (decisión de ingeniería de DEC-009, para no corromper citas ya reservadas).
- **EVIDENCIA:** `CatalogIntegrationTest.createsSpecialtyValidatesDurationAndKeepsCodeAndDurationImmutable`, `specialtiesManagementRequiresAdminRoleAndListIncludesInactive`; contrato en `docs/contracts/catalogs.md`; evidencia completa en `docs/evidence/goals-loops/S4/HU-006-HU-007-HU-008-implementation.md`.
- **VERIFICACIÓN:** verificación manual creó "Dermatología (prueba)" (60 min) y la desactivó contra backend y MySQL reales; `specialties` ya sembradas (Medicina General, Cardiología Adulto, Ortopedia y Traumatología) se listaron correctamente en la vista ADMIN.
- **PREGUNTA ABIERTA:** actualización formal del campo `estado` de HU-008 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`.

## HU-004 — Recuperar contraseña

- **HECHO:** 2026-10-03 — HU aprobada vía DEC-008 (duración 15 min, canal log del servidor, contrato REST completo). USER solicita recuperación por email (`202` siempre, sin revelar si la cuenta existe) y cambia su contraseña con un token de un solo uso.
- **EVIDENCIA:** `AuthIntegrationTest.requestingResetForActiveEmailIssuesSingleUseTokenOnlyInLogNeverInResponse`, `requestingResetForUnknownOrInactiveEmailStillRespondsAcceptedWithoutIssuingToken`, `confirmRejectsExpiredOrUnknownToken`, `passwordResetRequestAndConfirmValidateInput`; contrato en `docs/contracts/authentication.md`; evidencia completa en `docs/evidence/goals-loops/S4/HU-004-implementation.md`.
- **VERIFICACIÓN:** API con 33 pruebas en verde; frontend con typecheck, 34 pruebas Vitest y build correctos; verificación manual end-to-end en navegador real — token extraído del log real del proceso `mvn spring-boot:run`, login con contraseña nueva exitoso y con la anterior rechazado (`401`).
- **PREGUNTA ABIERTA:** actualización formal del campo `estado` de HU-004 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`.

## HU-002 — Registrar usuario

- **HECHO:** la evidencia automatizada backend valida registro, unicidad y hash de password mediante `AuthIntegrationTest`.
- **HECHO:** `docker compose exec -T citas-api-dev mvn clean test` terminó el 2026-09-22 con 5 pruebas, 0 fallos y 0 errores.
- **HECHO:** DEC-002 — el usuario aprobó explícitamente la interfaz gráfica de la pantalla de registro (línea base en `current-web-baseline.md`).
- **HECHO:** 2026-09-22 — smoke test manual contra el backend real (contenedor `fcv-citas-api-dev`, MySQL `fcv-citas-mysql`): `POST /api/v1/auth/register` crea un usuario real (`users.id=5`, rol `USER`) y `POST /api/v1/auth/register` con el mismo email responde `409 Email already registered` (CA-01, CA-02).
- **PREGUNTA ABIERTA:** falta demostración end-to-end integrada desde la pantalla de registro del cliente (navegador) contra el backend corriendo; el smoke test cubrió la API directamente, no la UI.

## HU-003 — Iniciar y cerrar sesión

- **HECHO:** la evidencia automatizada backend valida login, refresh con rotación, logout, CSRF, CORS y acceso protegido mediante `AuthIntegrationTest`.
- **HECHO:** el cliente React compila con `npm run build` y consume las rutas de registro/login del contrato de autenticación.
- **HECHO:** DEC-002 — el usuario aprobó explícitamente la interfaz gráfica de login (línea base en `current-web-baseline.md`).
- **HECHO:** 2026-09-22 — smoke test manual end-to-end contra la API real con el usuario `qa.login.demo@example.test`: `POST /api/v1/auth/login` devuelve access token + cookie `refresh_token` (HttpOnly) + `XSRF-TOKEN`; `GET /api/v1/auth/me` con el access token responde `{"subject":"5","roles":["ROLE_USER"]}` y sin token responde `401`; `POST /api/v1/auth/refresh` rota el refresh token; tras `POST /api/v1/auth/logout` un refresh con la cookie ya revocada responde `401` (CA-01, CA-02, CA-03 verificados contra la API).
- **RIESGO DETECTADO Y RESUELTO:** el proceso `mvn spring-boot:run` del contenedor `fcv-citas-api-dev` llevaba corriendo desde antes del último cambio de `UserEntity` (sin `created_at`); el runtime seguía usando metadata de Hibernate compilada con el `created_at` legado y devolvía `401 Authentication required` en registro/login por una `SQLSyntaxErrorException` interna. Se reinició el proceso (recompilado) y desapareció. **Nota operativa:** este contenedor no tiene reload en caliente; tras cambiar entidades/config hay que reiniciar `mvn spring-boot:run`, no solo guardar el archivo.
- **PREGUNTA ABIERTA:** falta demostración end-to-end desde la UI del cliente (navegador) — login/refresh/logout/me consumidos por `citas-web` en ejecución — y una decisión aprobada sobre el ciclo de vida del access token en el cliente.

## Riesgo de migración

- **DECISIÓN:** la transición Flyway conserva las tablas UUID anteriores como `legacy_*` y crea el esquema activo numérico compatible con el adaptador actual.
- **PREGUNTA ABIERTA:** antes de migrar un entorno con datos de usuarios reales o de laboratorio, debe aprobarse una estrategia de traslado de esos registros al esquema activo.

## Riesgo de versionado de migraciones

- **HECHO:** 2026-09-24 — las migraciones Flyway V1–V4 quedaron incorporadas al cambio S3; V3/V4 evolucionan el volumen legado y los volúmenes nuevos usan la baseline `4` del esquema de referencia.
- **PREGUNTA ABIERTA:** decidir si se commitea tal cual o se revisa antes, dado que S2 exige "BD conectada/migraciones iniciales" como entregable versionado.

## HU-015 — Reservar cita general

- **HECHO:** 2026-09-29 — HU aprobada y cerrada. La reserva general crea `APPROVED`, ocupa el slot, rechaza una segunda reserva con `409` y registra auditoría `SYSTEM`.
- **EVIDENCIA:** `BookingIntegrationTest.rejectsSecondReservationWithoutCreatingAnotherAppointment`; contrato en `docs/contracts/appointments.md`; evidencia Red→Green en `docs/evidence/S3-booking-red-green.md`.
- **VERIFICACIÓN:** API completa con 11 pruebas y frontend con typecheck, 3 pruebas Vitest y build correctos dentro de los contenedores Docker.

## HU-016 — Solicitar cita especializada

- **HECHO:** 2026-09-29 — HU aprobada y cerrada. Una solicitud nace `REQUESTED`, retiene todos los slots requeridos, exige dos slots consecutivos para 60 minutos y no permite una segunda solicitud sobre la franja retenida.
- **EVIDENCIA:** `BookingIntegrationTest.specializedRequestReservesTwoConsecutiveSlotsAndRejectsPartialDuration` y `BookingIntegrationTest.secondSpecializedReservationCannotUseHeldSlots`; contrato y evidencia S3.
- **DECISIÓN:** DEC-003 fija bloqueo pesimista, retención hasta decisión ADMIN y zona horaria `America/Bogota`.

## HU-017 — Decidir cita especializada

- **HECHO:** 2026-09-29 — HU aprobada y cerrada. ADMIN puede aprobar conservando slots o rechazar con motivo liberándolos; USER y PROFESSIONAL reciben `403`.
- **EVIDENCIA:** `BookingIntegrationTest.adminApprovesOrRejectsRequestedAppointmentsAndKeepsAudit` y `BookingIntegrationTest.userAndProfessionalCannotDecideSpecializedRequests`; contrato y evidencia S3.
- **HECHO:** las transiciones y su auditoría quedan verificadas mediante estado, actor, fuente, fecha y motivo aplicable.

## HU-022 — Consultar agenda profesional

- **DECISIÓN:** DEC-006 aprueba el inicio de implementación en el chat del goal S4 del 2026-09-29 (`decisiones.md`).
- **HECHO:** 2026-09-29 — PROFESSIONAL consulta únicamente sus citas `APPROVED`, filtrables por día/semana y sede opcional; agenda ajena queda vacía/denegada por ownership resuelto desde el JWT.
- **EVIDENCIA:** `BookingIntegrationTest.professionalSeesOnlyOwnApprovedAgendaFilteredByDayWeekAndLocation`, `BookingIntegrationTest.agendaAndClosureRequireProfessionalRole`; contrato en `docs/contracts/appointments.md`; verificación manual en navegador real contra backend y MySQL reales en `docs/evidence/goals-loops/S4/HU-022-HU-023-implementation.md`.
- **PREGUNTA ABIERTA:** actualización formal del campo `estado` a `Completada` en `docs/wiki/scrum/historias-de-usuario/HU-022-consultar-agenda-profesional.md` pendiente de `scrum-spec-orchestrator`.

## HU-023 — Cerrar atención

- **DECISIÓN:** DEC-006 fija "pasada/aplicable" = `endAt <= ahora` en `America/Bogota`, sin tolerancia, igual para 30 y 60 minutos.
- **HECHO:** 2026-09-29 — PROFESSIONAL marca una cita propia `APPROVED` y ya finalizada como `COMPLETED` o `NO_SHOW`; una cita futura, ajena o ya terminal no cambia (`409`/`404`); cada transición queda auditada con actor, fuente `USER` y fecha/hora del servidor.
- **EVIDENCIA:** `BookingIntegrationTest.professionalCanCloseEligiblePastApprovedAppointmentAndRecordsAudit`, `BookingIntegrationTest.cannotCloseFutureOrForeignAppointment`; contrato y evidencia de implementación; verificación manual con auditoría comprobada directamente en MySQL (`appointment_status_history`).
- **VERIFICACIÓN:** API con 25 pruebas en verde (confirmado en tres corridas consecutivas) y frontend con typecheck, 13 pruebas Vitest y build correctos dentro de los contenedores Docker.
- **PREGUNTA ABIERTA:** misma pendiente que HU-022 sobre el campo `estado` en `docs/wiki/scrum/`.

## HU-024 — Consultar bandeja administrativa

- **HECHO:** 2026-10-02 — HU aprobada vía DEC-007. ADMIN consulta solicitudes especializadas `REQUESTED` y reprogramaciones `PENDING` con filtros opcionales por sede, especialidad y fecha; roles no ADMIN reciben `403`.
- **EVIDENCIA:** `BookingIntegrationTest.adminInboxFiltersRequestedAppointmentsAndPendingReschedulesByLocationProfessionalSpecialtyAndDate`, `BookingIntegrationTest.adminInboxRequiresAdminRoleAndRejectsNonPositiveFilters`; contrato en `docs/contracts/appointments.md`; evidencia completa en `docs/evidence/goals-loops/S4/HU-024-HU-025-implementation.md`.
- **VERIFICACIÓN:** API con 29 pruebas en verde; frontend con typecheck, 29 pruebas Vitest y build correctos; verificación manual en navegador real contra backend y MySQL reales (filtro que excluye/incluye confirmado visualmente).
- **PREGUNTA ABIERTA:** actualización formal del campo `estado` de HU-024 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`.

## HU-025 — Consultar auditoría de estados

- **HECHO:** 2026-10-02 — HU aprobada vía DEC-007. La matriz de visibilidad es por ownership: ADMIN ve cualquier cita, USER la suya como paciente, PROFESSIONAL las que atendió; fuera de alcance responde `404` sin revelar si la cita existe. No existen endpoints de edición/eliminación de auditoría.
- **EVIDENCIA:** `BookingIntegrationTest.statusHistoryVisibleToAdminOwnerUserAndOwnerProfessionalButDeniedToOthers`, `BookingIntegrationTest.statusHistoryHasNoCrudEndpoints`; contrato en `docs/contracts/appointments.md`; evidencia completa en `docs/evidence/goals-loops/S4/HU-024-HU-025-implementation.md`.
- **VERIFICACIÓN:** verificación manual en navegador real confirmó ownership de ADMIN y de USER sobre su propia cita (historial completo con actor/fuente/motivo); el rol PROFESSIONAL quedó cubierto solo por la prueba de integración y revisión de código, por no contar con credenciales reales de las cuentas semilla `prof.*@demo.invalid`.
- **PREGUNTA ABIERTA:** actualización formal del campo `estado` de HU-025 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`.

## HU-014 — Consultar disponibilidad para reserva

- **HECHO:** 2026-09-29 — HU aprobada y cerrada. `GET /api/v1/availability` filtra por sede, especialidad, profesional y fecha, ofrece solo franjas completas de 30 o 60 minutos y excluye slots reservados por una cita o retenidos por una reprogramación `PENDING`.
- **EVIDENCIA:** `BookingIntegrationTest.userCanSearchOnlyCompleteAvailabilityUsingAllFilters`, `BookingIntegrationTest.heldRescheduleSlotIsNotOfferedNorReservable`; contrato en `docs/contracts/appointments.md`.
- **DECISIÓN:** DEC-003 ya fijaba concurrencia y retención; DEC-005 aprueba el cierre de la HU junto con HU-020/HU-021.

## HU-020 — Solicitar reprogramación

- **DECISIÓN:** DEC-005 aprueba la HU, descarta la subdivisión, fija la retención con la columna nueva `professional_slots.reschedule_request_id` y permite cambiar de sede entre las habilitadas del mismo profesional.
- **HECHO:** 2026-09-29 — USER con cita propia `APPROVED` y futura solicita una nueva franja completa del mismo profesional y especialidad; la solicitud nace `PENDING`, retiene la nueva franja y la cita original conserva franja, slots y estado. Cita ajena `404`, cambio de profesional/especialidad `400`, franja ocupada o solicitud duplicada `409`, cita `REQUESTED` `409`; ningún caso deja cambios parciales.
- **EVIDENCIA:** `BookingIntegrationTest.userRequestsReschedulePendingHoldsNewSlotAndKeepsOriginal`, `BookingIntegrationTest.rescheduleRequestRejectsForeignProfessionalChangeConflictAndIneligibleAppointment`, `BookingIntegrationTest.heldRescheduleSlotIsNotOfferedNorReservable`; migración `V7__add_reschedule_requests.sql`; `citas-web/src/components/MyAppointments.tsx` y `MyAppointments.test.tsx`.

## HU-021 — Decidir reprogramación

- **DECISIÓN:** DEC-005 fija que el rechazo exige motivo no vacío y la aprobación admite motivo opcional que se audita si se envía.
- **HECHO:** 2026-09-29 — ADMIN aprueba de forma atómica liberando los slots originales, confirmando la retención como reserva firme y moviendo la franja de la cita; o rechaza liberando la retención y conservando la cita original intacta. Rechazo sin motivo `400`, solicitud ya decidida `409`, USER y PROFESSIONAL `403`; ninguna respuesta de error muta datos.
- **EVIDENCIA:** `BookingIntegrationTest.adminApprovalSwapsSlotsAtomicallyAndKeepsAudit`, `BookingIntegrationTest.adminRejectionRequiresReasonReleasesHoldAndKeepsOriginalAppointment`, `BookingIntegrationTest.rescheduleInboxAndDecisionRequireAdminRoleAndRequestRequiresUserRole`; `citas-web/src/App.tsx` (`RescheduleInbox`) y `src/RescheduleInbox.test.tsx`.
- **HECHO:** cada decisión registra un evento en `appointment_status_history` con actor ADMIN, fuente `ADMIN`, fecha/hora del servidor y motivo aplicable, además de persistir decisor, fecha y motivo en la solicitud.
- **VERIFICACIÓN:** API `mvn -o test` con 25 pruebas, 0 fallos y 0 errores; frontend `npm run typecheck`, 23 pruebas Vitest y `npm run build` correctos, todo dentro de los contenedores Docker.
- **RIESGO RESUELTO:** la falla de `AuthIntegrationTest.corsAllowsOnlyConfiguredOriginWithCredentials` no era del alcance: la variable de entorno `APP_CORS_ALLOWED_ORIGINS` del contenedor tiene más precedencia que `src/test/resources/application.yml`. Se hizo la prueba hermética fijando la propiedad en su `@SpringBootTest`.
