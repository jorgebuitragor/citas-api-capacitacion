# Trazabilidad de historias de usuario

## HU-002 — Registrar usuario

- **HECHO:** la evidencia automatizada backend valida registro, unicidad y hash de password mediante `AuthIntegrationTest`.
- **HECHO:** `docker compose exec -T citas-api-dev mvn clean test` terminó el 2026-09-22 con 5 pruebas, 0 fallos y 0 errores.
- **HECHO:** DEC-002 — el usuario aprobó explícitamente la interfaz gráfica de la pantalla de registro (línea base en `current-web-baseline.md`).
- **PREGUNTA ABIERTA:** falta demostración end-to-end integrada de la pantalla de registro contra el backend corriendo.

## HU-003 — Iniciar y cerrar sesión

- **HECHO:** la evidencia automatizada backend valida login, refresh con rotación, logout, CSRF, CORS y acceso protegido mediante `AuthIntegrationTest`.
- **HECHO:** el cliente React compila con `npm run build` y consume las rutas de registro/login del contrato de autenticación.
- **HECHO:** DEC-002 — el usuario aprobó explícitamente la interfaz gráfica de login (línea base en `current-web-baseline.md`).
- **PREGUNTA ABIERTA:** faltan demostración end-to-end, refresh/logout/me en el cliente y una decisión aprobada sobre el ciclo de vida del access token.

## Riesgo de migración

- **DECISIÓN:** la transición Flyway conserva las tablas UUID anteriores como `legacy_*` y crea el esquema activo numérico compatible con el adaptador actual.
- **PREGUNTA ABIERTA:** antes de migrar un entorno con datos de usuarios reales o de laboratorio, debe aprobarse una estrategia de traslado de esos registros al esquema activo.
