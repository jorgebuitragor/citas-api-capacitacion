# Contrato REST — autenticación

Alcance: HU-002, HU-003 y HU-026. La API es la autoridad de autenticación; no existe BFF.

## Endpoints públicos

| Método y ruta | Entrada | Respuesta |
|---|---|---|
| `POST /api/v1/auth/register` | JSON con `firstName`, `lastName`, `documentType`, `documentNumber`, `email`, `phone`, `password` | `201` con `id`, `email`, `roles` |
| `POST /api/v1/auth/login` | JSON con `email`, `password` | `200` con `accessToken`, `tokenType=Bearer`, `accessExpiresAt`, `csrfToken`; cookies de sesión |
| `POST /api/v1/auth/refresh` | Cookie `refresh_token`, cookie `XSRF-TOKEN` y encabezado `X-CSRF-Token` coincidente | `200` con access token renovado y cookies rotadas |
| `POST /api/v1/auth/logout` | Mismas cookies/encabezado CSRF de refresh | `204`, revoca sesión y expira cookies |

`GET /api/v1/auth/me` requiere `Authorization: Bearer <accessToken>` y devuelve sujeto y roles. Es un recurso mínimo para comprobar el contexto de autorización.

Los identificadores se emiten como cadenas en JSON. Internamente corresponden a las claves numéricas `BIGINT` de `database/reference/db.sql`; este detalle no cambia el contrato del consumidor web.

## Sesión y seguridad

- Access JWT: 30 minutos; refresh: 14 días. Los secretos se aportan por `JWT_ACCESS_SECRET` y `JWT_REFRESH_SECRET`.
- El refresh nunca se entrega por JSON. `refresh_token` usa `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/api/v1/auth` y una duración de 14 días.
- `XSRF-TOKEN` no es HttpOnly para que el consumidor lo copie al encabezado `X-CSRF-Token` en refresh/logout. Ambas cookies rotan con cada refresh y se eliminan en logout.
- La API persiste solo el hash del refresh. Un refresh anterior queda revocado en la rotación y no puede reutilizarse.
- CORS permite credenciales solo para orígenes explícitos de `APP_CORS_ALLOWED_ORIGINS`; no se permiten comodines.

## Errores

Las respuestas de error usan `application/problem+json`: validación `400`, email/documento duplicado `409`, credenciales, token, refresh o CSRF inválidos `401`, y autorización insuficiente `403`. Los errores de credenciales no distinguen email, contraseña, token ni sesión inválidos.

El consumidor web queda pendiente: deberá conservar el access token según su diseño aprobado y enviar `credentials: include` y `X-CSRF-Token` solo en refresh/logout.
