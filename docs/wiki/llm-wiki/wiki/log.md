# Registro de la LLM Wiki

## [2026-09-17] scaffold | Inicialización

- Se creó el esquema y el índice inicial de la wiki global.
- No se ingirieron fuentes todavía.
- No se modificaron código, contratos, secretos ni la carpeta `scrum/`.

## [2026-09-22] decisión | Esquema de referencia como autoridad

- DEC-001 aprobada por el usuario: `database/reference/db.sql` se adopta como autoridad de persistencia.
- El backend se adapta a claves numéricas, `roles.code` y `refresh_tokens`; Flyway usa baseline `1` y no conserva el esquema paralelo de autenticación.
- Se conserva el contrato REST de IDs como texto y se actualizan las pruebas para usar MySQL inicializado desde el SQL de referencia.

## [2026-09-22] learn | Cierre técnico parcial de S2

- HECHO: se añadieron migraciones Flyway para alinear el esquema de autenticación activo con entidades numéricas y se validaron las pruebas de integración en el contenedor.
- HECHO: el cliente React compila y su URL de API usa la variable declarada en su `.env.example`.
- HECHO: una investigación acotada delegada revisó el consumo de sesión del cliente; confirma que refresh usa cookies HttpOnly/CSRF y deja como decisión pendiente el almacenamiento del access token.
- PREGUNTA ABIERTA: falta evidencia de ejecución integrada frontend/backend y aprobación explícita de la fuente visual.
- RIESGO: la transición conserva el esquema UUID previo como legado; una migración futura con datos requiere estrategia de traslado aprobada.

## [2026-09-22] decisión | Aprobación de interfaz gráfica de autenticación

- DEC-002 aprobada por el usuario: la interfaz gráfica actual de `citas-web` (pantallas de registro/login en `src/App.tsx`, línea base en `current-web-baseline.md`) queda aprobada como fuente visual.
- Cierra la pregunta abierta de HU-002/HU-003 sobre aprobación explícita de la fuente visual.
- Queda pendiente la demostración end-to-end integrada frontend/backend, que es un asunto distinto de la aprobación visual.

## [2026-09-22] learn | Smoke test de login contra backend real

- HECHO: se validó manualmente el ciclo completo register → login → `/me` → refresh → logout contra el backend real (`fcv-citas-api-dev`) y MySQL real (`fcv-citas-mysql`), con un usuario sintético creado por el propio flujo de registro (`users.id=5`, `qa.login.demo@example.test`). Todas las respuestas coincidieron con el contrato: 201, 200, 200, 200, 204, y 401 en refresh tras logout y en `/me` sin token.
- RIESGO DETECTADO Y RESUELTO: el proceso `mvn spring-boot:run` corría desde antes del último cambio en `UserEntity` y usaba metadata de Hibernate desactualizada (columna `created_at` legada), causando 401 espurios en registro/login. Se reinició el proceso; el contenedor no tiene hot-reload, así que cualquier cambio de entidades/config requiere reinicio manual.
- RIESGO: `citas-api/src/main/resources/db/` (migraciones Flyway V1/V2) está completamente sin trackear en git; el esquema activo depende de archivos no versionados.
- 2026-09-24 — DECISIÓN APROBADA: DEC-003 fija `America/Bogota`, retención REQUESTED hasta decisión y exclusión pesimista de slots; el contrato S3 se documenta en `docs/contracts/appointments.md`.
- 2026-09-24 — HECHO: la prueba roja inicial de doble reserva detectó que el entorno Flyway V2 carecía de `professional_slots`; V3/V4 materializan el modelo y la prueba verde valida el conflicto 409 sin cita duplicada. Evidencia: `docs/evidence/S3-booking-red-green.md`.

## [2026-09-29] cierre | S3 reservas end-to-end

- HECHO: HU-015, HU-016 y HU-017 fueron aprobadas y cerradas con sus CA/DoD y trazabilidad actualizados.
- HECHO: la revalidación Docker pasó 11 pruebas backend y typecheck, 3 pruebas Vitest y build frontend.

## [2026-09-29] pausa | Preflight HU-022/HU-023 (agenda profesional y cierre de atención)

- HECHO: se verificaron las condiciones de entrada del goal S4 para HU-022/HU-023. HU-015 y HU-017 (dependencias de reserva) están aprobadas y cerradas.
- PREGUNTA ABIERTA: HU-022 y HU-023 permanecen `Pendiente de aprobación`; ninguna decisión aprobada (DEC-001..DEC-003) define el criterio de "pasada/aplicable" que exige RF-17. HU-023 y EP-007 ya señalaban esta pregunta; se documentó formalmente en `riesgos-y-preguntas-abiertas.md`.
- FALTA: contrato REST de agenda profesional y de cierre de atención (no existen en `docs/contracts/appointments.md`); diseño aprobado de «dashboard PROFESSIONAL» y «agenda del profesional» (PRD §6).
- No se modificó código, esquema, contrato ni historias. Checkpoint completo en `docs/evidence/goals-loops/S4/HU-022-HU-023-preflight-checkpoint.md`.
- HECHO: se añadió una prueba específica de segunda solicitud especializada sobre slots retenidos.
- HECHO: V6 mantiene disponibilidad sintética futura en volúmenes persistentes sin editar migraciones ya aplicadas.
- EVIDENCIA: `docs/evidence/S3-booking-red-green.md` y `docs/contracts/appointments.md`.

## [2026-09-29] decisión | Aprobación de HU-022/HU-023 y criterio de "pasada/aplicable"

- DEC-006 aprobada por el usuario en el chat del goal S4: aprueba explícitamente el inicio de implementación de HU-022 y HU-023; para RF-17, "pasada/aplicable" = `endAt <= ahora` en `America/Bogota`, sin tolerancia, igual para 30 y 60 minutos.
- Cierra la pregunta abierta registrada en `riesgos-y-preguntas-abiertas.md` el mismo día.
- PREGUNTA ABIERTA remanente: la actualización formal del campo `estado` de HU-022/HU-023 en `docs/wiki/scrum/` requiere una ejecución de `scrum-spec-orchestrator`, no disponible en esta sesión; DEC-006 documenta la aprobación de negocio mientras tanto.
- Continúa: redacción de propuesta de contrato REST (agenda profesional, cierre de atención) y UI mínima para aprobación antes de implementar código de negocio.

## [2026-09-29] cierre | S4 HU-022/HU-023 agenda profesional y cierre de atención

- HECHO: contrato REST documentado y aprobado en `docs/contracts/appointments.md`; implementación backend (`BookingCommands`, `BookingPorts`, `BookingService`, `BookingPersistenceAdapter`, `BookingController`, `SecurityConfiguration`) y frontend (`ProfessionalAgenda.tsx`, `App.tsx`, `booking.ts`) completas.
- HECHO: 25 pruebas backend en verde (confirmado en tres corridas consecutivas tras descartar una colisión transitoria con el `mvn test` de la sesión concurrente) y 13 pruebas frontend con typecheck/build correctos, todas ejecutadas dentro de los contenedores Docker.
- HECHO: verificación manual end-to-end en navegador real contra el backend y MySQL reales, incluida limpieza completa de los datos sintéticos temporales usados. Evidencia detallada en `docs/evidence/goals-loops/S4/HU-022-HU-023-implementation.md`.
- RIESGO DETECTADO Y RESUELTO: el proceso `mvn spring-boot:run` de `fcv-citas-api-dev` llevaba corriendo desde antes de estos cambios (esquema V6) y no recarga en caliente; se reinició para servir el código actual (esquema V7). Mismo patrón operativo ya documentado para HU-002/HU-003.
- RIESGO DETECTADO Y RESUELTO: colisiones de edición concurrente con la sesión que implementaba HU-020/HU-021 sobre los mismos archivos (`BookingPorts.java`, `BookingCommands.java`, `decisiones.md` con IDs `DEC-004`/`DEC-005` duplicados dos veces). Se resolvieron releyendo cada archivo inmediatamente antes de escribir y renumerando la decisión propia a `DEC-006`.
- PREGUNTA ABIERTA: actualización formal del campo `estado` de HU-022/HU-023 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`.

## [2026-09-29] cierre | S4 HU-020/HU-021 reprogramación de citas

- HECHO: preflight documental registrado en `docs/evidence/goals-loops/S4/HU-020-HU-021-preflight-checkpoint.md`; el objetivo se pausó y escaló porque HU-014 no estaba cerrada, HU-020/HU-021 seguían sin aprobar y la regla de motivo del rechazo era una regla abierta.
- DECISIÓN: DEC-005 aprobada por el usuario: rechazo con motivo obligatorio y aprobación con motivo opcional; aprobación de HU-014, HU-020 y HU-021 sin subdivisión; retención mediante la columna nueva `professional_slots.reschedule_request_id`; sede modificable entre las habilitadas del mismo profesional.
- HECHO: contrato REST de reprogramación redactado como propuesta y aprobado por el usuario en `docs/contracts/appointments.md` (tres rutas nuevas más `rescheduleAllowed` y `rescheduleRequest` como extensión aditiva del objeto de cita de HU-018).
- HECHO: propuesta de diseño en `citas-web/docs/design/HU-020-HU-021-reprogramacion-proposal.md`; los flujos se integran en las pantallas ya aprobadas en lugar de crear rutas nuevas.
- HECHO: implementación cross-repo completa. API: migración idempotente `V7__add_reschedule_requests.sql`, dominio `RescheduleStatus`, puertos/casos de uso de solicitud y decisión, adaptador JDBC y tres endpoints. Web: diálogo de solicitud en `MyAppointments.tsx` y bandeja `RescheduleInbox` en `App.tsx`.
- HECHO: `mvn -o test` con 25 pruebas, 0 fallos y 0 errores; `npm run typecheck`, 23 pruebas Vitest y `npm run build` correctos; Flyway confirmó la versión 7 aplicada y la columna de retención creada.
- RIESGO DETECTADO Y RESUELTO: la falla CORS de `AuthIntegrationTest` que otra sesión había registrado como preexistente venía de que la variable de entorno `APP_CORS_ALLOWED_ORIGINS` del contenedor tiene más precedencia que el `application.yml` de pruebas. La prueba se hizo hermética fijando la propiedad en su `@SpringBootTest`.
- RIESGO DETECTADO Y RESUELTO: residuos de citas sintéticas en el MySQL compartido, dejados por ejecuciones de pruebas concurrentes de otra sesión, hacían fallar la aserción de conteo exacto de la agenda profesional. Se eliminaron los registros residuales y la suite quedó verde.
- RIESGO DETECTADO: dos sesiones editaron simultáneamente `BookingPorts.java`, `BookingCommands.java`, `BookingService.java`, `BookingController.java` y `decisiones.md`, y ejecutaron pruebas contra el mismo MySQL. Se mitigó releyendo cada archivo antes de escribir y renumerando decisiones; conviene no paralelizar HU que comparten los mismos archivos y base de datos.
- PREGUNTA ABIERTA: no se implementó cancelación automática tras rechazo, cambio de profesional, expiración de retenciones ni `patient_action_after_rejection`; siguen fuera de alcance hasta que existan HU y decisión propias.

## [2026-09-29] propuesta | S4 HU-024/HU-025 contrato REST

- HECHO: se documentó una propuesta aditiva para consultar la bandeja ADMIN de solicitudes `REQUESTED` y reprogramaciones `PENDING` con filtros opcionales por sede, profesional, especialidad y fecha.
- HECHO: se documentó `GET /api/v1/appointments/{appointmentId}/status-history` como consulta de solo lectura, sin endpoints CRUD de auditoría.
- DECISIÓN PROPUESTA: la matriz de visibilidad sería ADMIN sobre cualquier cita, USER sobre sus citas y PROFESSIONAL sobre sus citas; la autorización se mantiene pendiente de aprobación explícita antes de implementar.
- PREGUNTA ABIERTA: falta aprobar la matriz de visibilidad y el diseño específico de las pantallas antes de iniciar cambios backend/frontend.

## [2026-10-02] decisión | S4 HU-024/HU-025 aprobación y matriz de auditoría

- DECISIÓN: DEC-007 aprobada por el usuario: HU-024 (Consultar bandeja administrativa) aprobada sin cambios sobre su alcance y CA actuales. Para HU-025, se ratifica la matriz de visibilidad por ownership ya borrador en `docs/contracts/appointments.md:398` (ADMIN sobre cualquier cita, USER sobre sus propias citas, PROFESSIONAL sobre las que atendió), descartando la alternativa de restringir el endpoint solo a ADMIN que se evaluó primero en la misma conversación.
- HECHO: se cerró la pregunta abierta registrada en `riesgos-y-preguntas-abiertas.md` y en las notas de HU-025 ("Matriz exacta de lectura por rol pendiente de contrato").
- HECHO: queda habilitado el inicio de implementación de GOAL S4.04 (HU-024 y HU-025).
- PREGUNTA ABIERTA: actualización formal del campo `estado` de HU-024/HU-025 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`, mismo patrón ya registrado para HU-022/HU-023.

## [2026-10-02] cierre | S4 HU-024/HU-025 bandeja administrativa y auditoría

- HECHO: implementación cross-repo completa habilitada por DEC-007. API: filtros `locationId/professionalId/specialtyId/date` en `GET /api/v1/admin/appointments` y `GET /api/v1/admin/reschedule-requests`; nuevo `GET /api/v1/appointments/{id}/status-history` con autorización por ownership (ADMIN cualquiera, USER propia, PROFESSIONAL la que atendió) resuelta en `BookingService.statusHistory`; nuevo matcher de seguridad `hasAnyRole("ADMIN","USER","PROFESSIONAL")` para ese path, evaluado antes del matcher genérico USER. Web: componente compartido `StatusHistoryToggle` (carga diferida, sin refetch al colapsar/expandir) integrado en `AdminDashboard`, `RescheduleInbox`, `MyAppointments` y `ProfessionalAgenda`; `InboxFilterBar` (sede/especialidad/fecha) agregada a las dos bandejas ADMIN.
- HECHO: `docs/contracts/appointments.md` actualizado de "PROPUESTA — pendiente de aprobación" a "aprobado — DEC-007".
- HECHO: `mvn -o test` con 29 pruebas, 0 fallos y 0 errores (25 preexistentes sin regresión + 4 nuevas); `npm run test` con 29 pruebas en verde (23 preexistentes, con los mocks de `RescheduleInbox.test.tsx` ajustados para las nuevas llamadas de catálogos, + 6 nuevas de `StatusHistory.test.tsx`); `npm run typecheck` y `npm run build` limpios.
- HECHO: verificación manual end-to-end en navegador real contra backend y MySQL reales — cuenta de prueba creada vía registro real y promovida a ADMIN por SQL (conservando también USER), solicitud especializada creada, filtros de bandeja probados (excluye/incluye), historial verificado para ADMIN y para USER propietario a través de tres pantallas distintas, incluida una reprogramación real. Datos sintéticos eliminados al cerrar.
- RIESGO DETECTADO Y RESUELTO: el endpoint ya existente `GET /api/v1/admin/appointments` (reutilizado de HU-017) tenía un contrato borrador con objetos anidados `{id,name}` para profesional/especialidad/sede, pero la implementación real y el frontend ya enviado (`PendingAppointment` en `citas-web`) usan campos planos (`professionalName`, etc.). Se mantuvo el shape plano ya probado por HU-017 (sin romper su pantalla de decisión) y solo se agregaron los filtros como parámetros de consulta adicionales, en vez de reescribir la respuesta para igualar el borrador del contrato.
- PREGUNTA ABIERTA: no se verificó manualmente en navegador el toggle de historial para el rol PROFESSIONAL por no contar con credenciales reales de las cuentas semilla `prof.*@demo.invalid` (`active = FALSE`); cubierto por prueba de integración y revisión de código. El filtro `professionalId` de la bandeja existe en la API pero no se expuso en la UI de esta iteración.
- PREGUNTA ABIERTA: actualización formal del campo `estado` de HU-024/HU-025 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`.

## [2026-10-02] cierre | S4 HU-024/HU-025 marcadas Completada por instrucción directa del usuario

- HECHO: el usuario instruyó explícitamente marcar HU-024 y HU-025 como `Completada`. Se actualizó `docs/wiki/scrum/historias-de-usuario/HU-024-consultar-bandeja-administrativa.md` y `HU-025-consultar-auditoria-de-estados.md`: `estado: Completada`, tareas y DoD marcados, tabla de evidencia de validación llenada por CA con referencia a las pruebas y a `docs/evidence/goals-loops/S4/HU-024-HU-025-implementation.md`, e historial de validación extendido.
- HECHO: `docs/wiki/scrum/README.md` — HU-024 y HU-025 agregadas a "HU aprobadas para selección". `EP-008-operacion-administrativa-y-trazabilidad.md` — `estado: Completada`, criterio de completitud marcado, riesgo de matriz de visibilidad cerrado con referencia a DEC-007.
- RIESGO ACEPTADO: `AGENTS.md` reserva la escritura en `docs/wiki/scrum/` a la skill `scrum-spec-orchestrator`. Esta vez se escribió directamente por instrucción explícita del usuario en el chat, documentada también en DEC-007. No se interpreta como cambio de la regla general para sesiones futuras.

## [2026-10-02] cierre | S4 HU-022/HU-023 marcadas Completada por instrucción directa del usuario

- HECHO: al analizar qué faltaba de S4 a pedido del usuario, se detectó que GOAL S4.03 (HU-022/HU-023) tenía la misma inconsistencia que acababa de corregirse en HU-024/HU-025: implementadas y verificadas desde el 2026-09-29 (`docs/evidence/goals-loops/S4/HU-022-HU-023-implementation.md`), pero con `estado: Pendiente de aprobación` sin actualizar.
- HECHO: el usuario instruyó corregirlo. Se actualizó `docs/wiki/scrum/historias-de-usuario/HU-022-consultar-agenda-profesional.md` y `HU-023-cerrar-atencion.md`: `estado: Completada`, tareas y DoD marcados, tabla de evidencia llenada con referencia a `BookingIntegrationTest` (pruebas de la sesión original) y al documento de evidencia, e historial de validación extendido.
- HECHO: `docs/wiki/scrum/README.md` — HU-022 y HU-023 agregadas a "HU aprobadas para selección". `EP-007-operacion-clinica-del-profesional.md` — `estado: Completada`, criterio de completitud marcado, riesgo de "pasada/aplicable" cerrado con referencia a DEC-006.
- RIESGO ACEPTADO: misma excepción puntual a `AGENTS.md` ya documentada para HU-024/HU-025 — escritura directa en `docs/wiki/scrum/` por instrucción explícita del usuario, no por `scrum-spec-orchestrator`.
- HECHO: con esto, los seis GOAL de S4 quedan con su estado real reflejado en `docs/wiki/scrum/`: S4.01–S4.04 formalmente `Completada`; S4.05 (HU-004) y S4.06 (HU-006/007/008) siguen sin implementar, bloqueados por decisiones de negocio pendientes (duración/canal del token de recuperación; unicidad de EPS, definición de "plan activo" y seed de Medicina General).

## [2026-10-03] cierre | S4 HU-004 recuperación de contraseña

- DECISIÓN: DEC-008 aprobada por el usuario: token de 15 minutos, canal de desarrollo = log del servidor (RF-03 del PRD autoriza log/respuesta controlada; se eligió log para no filtrar el token en la respuesta HTTP), y contrato REST completo (`202` siempre en `request`, `401` genérico en `confirm`).
- HECHO: implementación cross-repo completa. API: migración `V8__add_password_reset_tokens.sql`; dominio `PasswordResetToken`; puerto `PasswordResetRepository` y `UserRepository.updatePassword`; `AuthService.requestPasswordReset`/`confirmPasswordReset` (token aleatorio de 32 bytes vía `SecureRandom`, hash SHA-256 persistido, comparación de tiempo constante al confirmar); dos endpoints públicos en `AuthController`; TTL configurable (`app.security.password-reset-ttl=PT15M`). Web: `ForgotPasswordScreen` y `ResetPasswordScreen` reutilizando el estilo de login/registro ya aprobado (DEC-002), enlace "¿Olvidaste tu contraseña?" en el login.
- HECHO: `docs/contracts/authentication.md` ampliado con la sección "Recuperación de contraseña (HU-004, DEC-008)".
- HECHO: `mvn -o test` con 33 pruebas, 0 fallos y 0 errores (29 preexistentes sin regresión + 4 nuevas); `npm run test` con 34 pruebas en verde (29 preexistentes + 5 nuevas); `npm run typecheck` y `npm run build` limpios.
- HECHO: verificación manual end-to-end en navegador real — cuenta de prueba creada vía registro real, token extraído del log real del proceso `mvn spring-boot:run` (no de la respuesta HTTP), contraseña actualizada, login con la contraseña anterior rechazado (`401`) y con la nueva exitoso. Datos sintéticos eliminados al cerrar.
- RIESGO DETECTADO Y RESUELTO: el helper compartido `request(...)` de `citas-web/src/api/auth.ts` solo trataba `204` como respuesta sin cuerpo; los nuevos endpoints responden `202`/`200` sin body y `.json()` fallaba al parsear una cadena vacía. Se generalizó el helper para tratar cualquier cuerpo vacío como `undefined` sin importar el código de estado — cambio retrocompatible, verificado por las pruebas de login/registro ya existentes.
- RIESGO DETECTADO Y RESUELTO (operativo, no de código): al reiniciar `mvn spring-boot:run` para la verificación manual, un primer intento con `docker compose exec -T ... | tail` perdió la captura de logs tras el límite de tiempo en segundo plano del propio entorno de ejecución, y un segundo intento se lanzó sin matar el proceso anterior, chocando por el puerto 8080 dentro del contenedor. Se resolvió matando ambos procesos duplicados y relanzando con la salida redirigida explícitamente a un archivo (`exec > /tmp/api.log 2>&1`), lo que además permitió capturar el token logueado de forma confiable con un `grep` normal en vez de leer `/proc/<pid>/fd/1` (que no es releíble: es un pipe en vivo, no un archivo).
- PREGUNTA ABIERTA: actualización formal del campo `estado` de HU-004 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`.

## [2026-10-03] cierre | S4 HU-006/HU-007/HU-008 catálogos configurables — último GOAL de S4

- DECISIÓN: DEC-009 aprobada por el usuario. La pregunta de unicidad de HU-006 se resolvió sin preguntar, siguiendo la autoridad ya fijada en DEC-001 (`database/reference/db.sql`): `eps.code` único, `eps_plans` único por `(eps_id, code)`. Se aprobó traer `insurance_regimes`/`eps`/`eps_plans` tal cual el esquema de referencia, CRUD ADMIN para los tres catálogos, desactivación en vez de borrado físico, y la inmutabilidad de `code` (y de `durationMinutes`/`general`/`requiresAdminApproval` en especialidades) tras crear — decisión de ingeniería, no pedida explícitamente por la HU.
- HECHO: implementación cross-repo completa. API: migración `V9__add_insurance_regimes_eps_and_plans.sql`; nuevo módulo hexagonal `application/catalog` (puerto/comandos/excepción/servicio) siguiendo el patrón de `application/booking`; `CatalogPersistenceAdapter` (JDBC crudo); `CatalogController` nuevo bajo `/api/v1/admin/catalogs/**` (ADMIN, matcher ya existente) y `/api/v1/catalogs/insurance-regimes` (USER, matcher ya existente) — sin cambios de seguridad. Web: `CatalogAdmin.tsx` (`CatalogsDashboard`) con tres secciones (EPS, planes anidados por EPS, especialidades), nueva ruta `catalogs` y botón en `AdminDashboard`; se exportaron `Field`/`NoticeBox`/`PortalLayout`/`Notice` desde `App.tsx` para reutilizarlos sin duplicar código.
- HECHO: nuevo contrato `docs/contracts/catalogs.md`.
- HECHO: `mvn -o test` con 41 pruebas, 0 fallos y 0 errores (33 preexistentes sin regresión + 8 nuevas); `npm run test` con 39 pruebas en verde (34 preexistentes + 5 nuevas); `npm run typecheck` y `npm run build` limpios.
- HECHO: verificación manual end-to-end en navegador real — cuenta de prueba creada vía registro real y promovida a ADMIN por SQL, pantalla de Catálogos cargó los 3 EPS/5 planes/3 especialidades ya sembrados por la migración, se creó y desactivó un plan real y una especialidad real contra backend y MySQL reales. Datos sintéticos eliminados al cerrar.
- RIESGO DETECTADO Y RESUELTO: el preflight CORS (`OPTIONS`) rechazaba `PATCH` con `403` — `SecurityConfiguration.corsConfigurationSource` solo permitía `GET, POST, OPTIONS` desde que el proyecto empezó (ningún endpoint anterior había necesitado `PATCH`; las actualizaciones previas, como la decisión de solicitudes, usaban `POST .../decision`). Corregido agregando `"PATCH"` a `allowedMethods`. Se agregó `CatalogIntegrationTest.corsAllowsPatchForConfiguredOrigin` como prueba de regresión — ninguna prueba anterior lo detectaba porque `MockMvc` sin encabezado `Origin` no dispara el filtro CORS; solo un preflight real (navegador u `OPTIONS` explícito con `Access-Control-Request-Method`) lo expone. Este riesgo aplica a cualquier `PATCH`/método nuevo que se agregue en el futuro: conviene revisar `allowedMethods` cada vez.
- PREGUNTA ABIERTA: actualización formal del campo `estado` de HU-006/HU-007/HU-008 en `docs/wiki/scrum/` pendiente de `scrum-spec-orchestrator`.
- HECHO: con esto, **los seis GOAL de S4 (S4.01–S4.06) quedan implementados y verificados.**

## [2026-10-03] verificación | Prueba end-to-end integral de S4 y cierre formal en Scrum

- HECHO: prueba end-to-end de todo lo implementado hasta S4.06 contra backend y MySQL reales, con los tres roles: 57/57 verificaciones API y recorrido manual por la UI de USER, ADMIN y PROFESSIONAL. Evidencia en `docs/evidence/goals-loops/S4/E2E-2026-10-03.md`. Suites: backend 43/43, frontend 39/39.
- RIESGO DETECTADO Y RESUELTO: en servidor real, los errores del framework (400/404/405 vía `sendError`) salían como `401` porque el redespacho `ERROR` caía en `anyRequest().authenticated()`; `MockMvc` no lo reproduce. Corregido con `dispatcherTypeMatchers(ERROR).permitAll()` y `ErrorDispatchIntegrationTest` (servidor real; verificado que falla sin la corrección).
- HECHO: cierra el pendiente de verificación manual del historial para PROFESSIONAL (HU-025), comprobado en la UI del profesional.
- HECHO: el usuario instruyó marcar HU-004, HU-006, HU-007 y HU-008 como `Completada` en `docs/wiki/scrum/` (misma excepción puntual a `AGENTS.md` ya registrada para HU-022–025). Se actualizó el estado, las tareas, el DoD, la tabla de evidencia y el historial de cada una, el estado de EP-002 y la lista de HU aprobadas del README del backlog.
- PREGUNTA ABIERTA: HU-007 CA-02 (rechazar un plan ajeno al afiliarse) queda `Parcial` — depende de HU-005 (afiliación), que no está implementada; la pertenencia plan/EPS sí está garantizada por FK y ruta anidada.

## [2026-10-04] avance | S5.01 WF-001 recordatorios — parte verificable sin n8n

- DECISIÓN: DEC-010 aprobada por el usuario (ventana 24 h, destinatario = email sintético, deduplicación por cita y franja, fuente REST nueva con API key de servicio; n8n del trainer; Mailpit + nodo Gmail listo; S5.02 con escenario simulado).
- HECHO: módulo `application/automation` (solo lectura de citas `APPROVED` próximas y registro de resultado), `ApiKeyAuthenticationFilter` (rol `AUTOMATION`, solo `/api/v1/automation/**`), migración `V10__add_appointment_reminders.sql`, `docs/contracts/automation.md`, `WF-001-appointment-reminders.json` (importable, `active: false`, sin secretos), Mailpit opcional y `AUTOMATION_API_KEY` en `docker-compose.yml`/`.env.example`.
- HECHO: backend 49/49. Réplica nodo a nodo del workflow contra la API real y Mailpit: 9/9. Evidencia en `docs/evidence/goals-loops/S5/S5-01-WF001-iteracion-1.md`.
- RIESGO DETECTADO Y RESUELTO: la suite de reservas fallaba (20/24) al pasar los días porque la disponibilidad sintética solo existe "para mañana" respecto de la fecha de la migración; `BookingIntegrationTest` reaplica el script idempotente de V6 antes de cada caso.
- PREGUNTA ABIERTA: S5.01 queda PAUSADO — falta acceso al n8n del trainer (URL, MCP), una URL de la API alcanzable desde él y credenciales (Gmail/SMTP) que debe crear el usuario. No verificados: importación y ejecución en n8n, invocación MCP, envío por Gmail. HU-028 no se marca `Completada`.
- PREGUNTA ABIERTA: S5.02 (contenido no confiable) pendiente; se hará con un escenario simulado.

