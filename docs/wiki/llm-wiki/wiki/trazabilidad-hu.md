# Trazabilidad de historias de usuario

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
