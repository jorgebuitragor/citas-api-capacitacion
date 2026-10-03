# S4 — Evidencia de implementación HU-004 (recuperación de contraseña)

- **Fecha:** 2026-10-03.
- **Resultado:** **IMPLEMENTADO Y VERIFICADO** tras la aprobación explícita del usuario en chat (DEC-008 en `docs/wiki/llm-wiki/wiki/decisiones.md`), que habilitó GOAL S4.05.
- **Repositorios modificados:** `citas-api` y `citas-web`, ambos en `develop`.

## Decisión que habilitó la implementación

- **DEC-008** (`decisiones.md`): el usuario aprobó duración del token (**15 minutos**), canal de entrega en desarrollo (**log del servidor**, autorizado por RF-03 del PRD) y el contrato REST completo (`202` siempre en `request` para no revelar qué emails existen; `401` genérico en `confirm` para cualquier token inválido/vencido/usado).
- Cierra la pregunta abierta registrada en las notas de HU-004 ("Duración y canal exactos son pregunta abierta").

## Contrato REST

`docs/contracts/authentication.md`, nueva sección "Recuperación de contraseña (HU-004, DEC-008)":

- `POST /api/v1/auth/password-reset/request` con `{ "email" }` — público, responde `202` siempre.
- `POST /api/v1/auth/password-reset/confirm` con `{ "token", "newPassword" }` — público, `200` o `401` genérico.

## Backend (`citas-api`)

- Migración `V8__add_password_reset_tokens.sql`: tabla `password_reset_tokens` (mismo esquema que `refresh_tokens` — solo se persiste `token_hash`, nunca el valor en texto plano).
- `domain/auth/PasswordResetToken.java` (nuevo): record con `isActiveAt(now)`, igual patrón que `RefreshSession`.
- `AuthPorts`: nueva interfaz `PasswordResetRepository` (`save`, `findByTokenHash`, `consume`); `UserRepository` gana `updatePassword(userId, hash)`.
- `AuthCommands`: `PasswordResetRequestCommand`, `PasswordResetConfirmCommand`.
- `AuthException.Reason.INVALID_RESET_TOKEN` — cae en la rama `default` ya existente de `ApiExceptionHandler` (`401`, "Authentication failed"), sin necesidad de tocar el exception handler.
- `AuthService`: `requestPasswordReset(...)` no revela si el email existe (responde igual, sin excepción, exista o no la cuenta); si el usuario existe y está activo, genera un token aleatorio de 32 bytes (`SecureRandom` + Base64 URL-safe, mismo patrón que el CSRF token de `AuthController`), lo persiste como hash SHA-256 con expiración de 15 minutos, y lo escribe en el log vía SLF4J con la marca `[DEV]`. `confirmPasswordReset(...)` valida vigencia/no-consumo con comparación de tiempo constante (mismo patrón que `refresh`), hashea la nueva contraseña con BCrypt y consume el token en la misma transacción.
- `AuthPersistenceAdapter`: nueva entidad JPA `PasswordResetTokenEntity` (tabla `password_reset_tokens`) y repositorio Spring Data; `updatePassword` reutiliza `UserJpaRepository.findById` + `save` (upsert por id, no crea un usuario nuevo).
- `AuthController`: dos endpoints nuevos sin autenticación previa, agregados al `permitAll()` de `SecurityConfiguration`.
- `AuthConfiguration`: nuevo `@Value("${app.security.password-reset-ttl}")` (`PT15M` en `application.yml` y en `src/test/resources/application.yml`).

### Pruebas backend

`AuthIntegrationTest` — 4 pruebas nuevas:

- `requestingResetForActiveEmailIssuesSingleUseTokenOnlyInLogNeverInResponse` — captura el token con un `ListAppender` de Logback enganchado *antes* de la petición (para no perder el evento), confirma que la respuesta HTTP no trae body, confirma con el token capturado, verifica login con la contraseña nueva y rechazo con la antigua, y confirma que reusar el mismo token una segunda vez falla.
- `requestingResetForUnknownOrInactiveEmailStillRespondsAcceptedWithoutIssuingToken` — email desconocido e inactivo responden `202` sin generar ningún log `[DEV]`.
- `confirmRejectsExpiredOrUnknownToken` — vence el token directamente en BD y confirma `401`; token nunca emitido también `401`; el login con la contraseña original sigue funcionando (nada mutó).
- `passwordResetRequestAndConfirmValidateInput` — email inválido y campos vacíos responden `400`.

**Ejecución real en contenedor** (`docker compose exec citas-api-dev mvn -o test`):

```
Tests run: 33, Failures: 0, Errors: 0, Skipped: 0
```

Las 33 incluyen las 29 preexistentes (S3, HU-018–025) sin regresiones, más las 4 nuevas.

## Frontend (`citas-web`)

- `src/api/auth.ts`: `requestPasswordReset(email)` y `confirmPasswordReset({ token, newPassword })`. Se corrigió el helper `request(...)` compartido: antes solo trataba `204` como cuerpo vacío y fallaría al parsear JSON sobre las respuestas `202`/`200` sin body de estos nuevos endpoints; ahora trata cualquier cuerpo vacío como `undefined` independientemente del status, cambio retrocompatible verificado por las pruebas existentes de login/registro.
- `src/App.tsx`: pantallas nuevas `ForgotPasswordScreen` y `ResetPasswordScreen`, reutilizando sin cambios las clases CSS y el patrón de `Field`/`NoticeBox` ya aprobados para login/registro (DEC-002) — sin rediseño. Enlace "¿Olvidaste tu contraseña?" agregado al formulario de login existente.

### Pruebas frontend

`src/PasswordReset.test.tsx` (nuevo) — 5 pruebas: solicitud exitosa sin revelar si el email existe, error de servidor sin romper con el body vacío de éxito, validación de contraseñas no coincidentes antes de llamar a la API, confirmación exitosa con el payload correcto, y error genérico para token inválido/vencido.

**Ejecución real en contenedor** (`docker compose exec citas-web-dev npm run test`):

```
Test Files  6 passed (6)
     Tests  34 passed (34)
```

`npm run typecheck` y `npm run build` — ambos limpios, sin errores.

## Verificación manual end-to-end (navegador real contra backend y MySQL reales)

1. Se creó una cuenta de prueba (`pedro.reset@example.test`) desde el formulario de registro real de la UI.
2. Desde el login, el enlace "¿Olvidaste tu contraseña?" lleva a la pantalla de solicitud. Se envió el email y la UI mostró "Si el correo está registrado, se generó un token de recuperación válido por 15 minutos." — la respuesta de red (`POST .../password-reset/request` → `202`) se confirmó con body vacío.
3. El token en texto plano se recuperó del log real del proceso `mvn spring-boot:run` corriendo en el contenedor `fcv-citas-api-dev` (nunca de la respuesta HTTP ni de la UI).
4. En la pantalla de confirmación, se pegó el token y se definió una nueva contraseña. La UI mostró "Contraseña actualizada. Ya puedes iniciar sesión con la nueva contraseña."
5. Login con la contraseña **original** → `401 Authentication failed` (rechazado correctamente).
6. Login con la contraseña **nueva** → éxito, aterrizó en "Mis citas".
7. **Limpieza:** se eliminaron el token de recuperación, los refresh tokens, el rol y el usuario de prueba de MySQL real; no quedan datos de esta verificación en el entorno compartido.

## Criterios de aceptación — estado

| Criterio | Evidencia | Estado |
|---|---|---|
| CA-01 (solicitud controlada, token de un uso por el canal autorizado) | `requestingResetForActiveEmailIssuesSingleUseTokenOnlyInLogNeverInResponse`; verificación manual pasos 2–3 | Cumplido |
| CA-02 (cambio con token válido, hash y token inutilizable) | Mismo test (login con nueva contraseña, reuso del token falla); verificación manual pasos 4–6 | Cumplido |
| CA-03 (token no reutilizable: vencido/consumido/inválido rechazado) | `confirmRejectsExpiredOrUnknownToken`; mismo test de reuso | Cumplido |
| DoD — ningún password/token expuesto fuera del canal de desarrollo autorizado | Respuesta HTTP de `request` siempre vacía (verificado en test y manualmente); el token solo existe en el log del servidor | Cumplido |
| DoD — no revelar qué emails existen | `requestingResetForUnknownOrInactiveEmailStillRespondsAcceptedWithoutIssuingToken` | Cumplido |

## Resultado

- **PASADO.** 33/33 pruebas backend y 34/34 pruebas frontend en verde, typecheck y build limpios, y verificación manual end-to-end exitosa contra el backend y MySQL reales, incluyendo la comprobación negativa de la contraseña anterior.
- Pendiente para cerrar formalmente el ciclo Scrum: ejecución de `scrum-spec-orchestrator` para marcar HU-004 `Completada` con CA/DoD y trazabilidad, mismo patrón ya documentado para HU-022/023 y HU-024/025 — salvo que el usuario vuelva a pedir la actualización directa, como hizo con esas HU.
