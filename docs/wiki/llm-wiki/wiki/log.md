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
