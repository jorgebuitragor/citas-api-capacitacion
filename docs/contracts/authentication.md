# Contrato REST — autenticación

Alcance: HU-002, HU-003, HU-004 y HU-026. La API es la autoridad de autenticación; no existe BFF.

## Endpoints públicos

| Método y ruta | Entrada | Respuesta |
|---|---|---|
| `POST /api/v1/auth/register` | JSON con `firstName`, `lastName`, `documentType`, `documentNumber`, `email`, `phone`, `password` | `201` con `id`, `email`, `roles` |
| `POST /api/v1/auth/login` | JSON con `email`, `password` | `200` con `accessToken`, `tokenType=Bearer`, `accessExpiresAt`, `csrfToken`; cookies de sesión |
| `POST /api/v1/auth/refresh` | Cookie `refresh_token`, cookie `XSRF-TOKEN` y encabezado `X-CSRF-Token` coincidente | `200` con access token renovado y cookies rotadas |
| `POST /api/v1/auth/logout` | Mismas cookies/encabezado CSRF de refresh | `204`, revoca sesión y expira cookies |
| `POST /api/v1/auth/password-reset/request` | JSON con `email` | `202` siempre (ver abajo) |
| `POST /api/v1/auth/password-reset/confirm` | JSON con `token`, `newPassword` | `200` si el token es válido; `401` si no |

`GET /api/v1/auth/me` requiere `Authorization: Bearer <accessToken>` y devuelve sujeto y roles. Es un recurso mínimo para comprobar el contexto de autorización.

Los identificadores se emiten como cadenas en JSON. Internamente corresponden a las claves numéricas `BIGINT` de `database/reference/db.sql`; este detalle no cambia el contrato del consumidor web.

## Recuperación de contraseña (HU-004, DEC-008)

- `password-reset/request` responde `202 Accepted` sin excepción, exista o no el email — nunca revela qué cuentas están registradas. Si el email corresponde a un usuario activo, el servidor crea un token de un solo uso (expira en **15 minutos**) y lo escribe en el log de `citas-api` con la marca `[DEV]`; no hay envío real de correo (RF-03 del PRD lo autoriza explícitamente para el entorno de laboratorio).
- El token nunca viaja en la respuesta HTTP de `request`; solo existe en el log del servidor y en el enlace/formulario que el usuario usa para llamar a `confirm`.
- `password-reset/confirm` hashea la nueva contraseña con BCrypt (mismo algoritmo que `register`) y marca el token consumido en la misma transacción. Un token vencido, ya consumido o desconocido responde `401` igual que una credencial inválida — sin distinguir el motivo ante el cliente.
- La API persiste solo el hash SHA-256 del token (`password_reset_tokens.token_hash`), nunca el valor en texto plano, mismo patrón que `refresh_tokens`.
- Fuera de alcance: no se revoca la sesión activa del usuario al cambiar la contraseña ni se envían notificaciones adicionales.

## Sesión y seguridad

- Access JWT: 30 minutos; refresh: 14 días. Los secretos se aportan por `JWT_ACCESS_SECRET` y `JWT_REFRESH_SECRET`.
- El refresh nunca se entrega por JSON. `refresh_token` usa `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/api/v1/auth` y una duración de 14 días.
- `XSRF-TOKEN` no es HttpOnly para que el consumidor lo copie al encabezado `X-CSRF-Token` en refresh/logout. Ambas cookies rotan con cada refresh y se eliminan en logout.
- La API persiste solo el hash del refresh. Un refresh anterior queda revocado en la rotación y no puede reutilizarse.
- CORS permite credenciales solo para orígenes explícitos de `APP_CORS_ALLOWED_ORIGINS`; no se permiten comodines.

## Errores

Las respuestas de error usan `application/problem+json`: validación `400`, email/documento duplicado `409`, credenciales, token, refresh o CSRF inválidos `401`, y autorización insuficiente `403`. Los errores de credenciales no distinguen email, contraseña, token ni sesión inválidos.

El consumidor web queda pendiente: deberá conservar el access token según su diseño aprobado y enviar `credentials: include` y `X-CSRF-Token` solo en refresh/logout.
