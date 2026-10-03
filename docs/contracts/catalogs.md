# Contrato REST — catálogos configurables

Alcance: HU-006, HU-007, HU-008 (DEC-009). Ninguna operación de este contrato borra físicamente un catálogo referenciado (RF-06); solo `active` se alterna. Los identificadores se emiten como cadenas en JSON.

## Régimen de afiliación (catálogo fijo, RF-05)

| Método y ruta | Rol | Respuesta |
|---|---|---|
| `GET /api/v1/catalogs/insurance-regimes` | USER | `200` con `[{ "id", "code", "name" }]` |

## EPS (HU-006)

| Método y ruta | Rol | Entrada | Respuesta |
|---|---|---|---|
| `GET /api/v1/admin/catalogs/eps` | ADMIN | — | `200` con `[{ "id", "code", "name", "active" }]` (incluye inactivas) |
| `POST /api/v1/admin/catalogs/eps` | ADMIN | `{ "code", "name" }` | `201` con el objeto creado |
| `PATCH /api/v1/admin/catalogs/eps/{id}` | ADMIN | `{ "name"?, "active"? }` | `200` con el objeto actualizado |

`code` es inmutable tras la creación y único (`eps.code UNIQUE` en el esquema de referencia); un código repetido responde `409`.

## Planes de EPS (HU-007)

| Método y ruta | Rol | Entrada | Respuesta |
|---|---|---|---|
| `GET /api/v1/admin/catalogs/eps/{epsId}/plans` | ADMIN | — | `200` con `[{ "id", "epsId", "code", "name", "active", "regime": { "id", "code", "name" } }]` |
| `POST /api/v1/admin/catalogs/eps/{epsId}/plans` | ADMIN | `{ "regimeId", "code", "name" }` | `201` con el objeto creado |
| `PATCH /api/v1/admin/catalogs/eps/{epsId}/plans/{planId}` | ADMIN | `{ "name"?, "active"? }` | `200` con el objeto actualizado |

`code` es único por EPS (`eps_plans` único por `(eps_id, code)`), no globalmente; un código repetido dentro de la misma EPS responde `409`. `regimeId` debe existir en `insurance-regimes`; uno inexistente responde `400`. `epsId` inexistente responde `404` en las tres rutas.

## Especialidades (HU-008)

| Método y ruta | Rol | Entrada | Respuesta |
|---|---|---|---|
| `GET /api/v1/admin/catalogs/specialties` | ADMIN | — | `200` con `[{ "id", "code", "name", "durationMinutes", "general", "requiresAdminApproval", "active" }]` (incluye inactivas; vista distinta de `GET /api/v1/catalogs/specialties`, que solo expone activas para reserva) |
| `POST /api/v1/admin/catalogs/specialties` | ADMIN | `{ "code", "name", "durationMinutes", "general", "requiresAdminApproval" }` | `201` con el objeto creado |
| `PATCH /api/v1/admin/catalogs/specialties/{id}` | ADMIN | `{ "name"?, "active"? }` | `200` con el objeto actualizado |

`durationMinutes` solo admite `30` o `60` (`ck_specialty_duration`); otro valor responde `400`. `code` es único; repetido responde `409`. `code`, `durationMinutes`, `general` y `requiresAdminApproval` son inmutables tras la creación — cambiar la duración retroactivamente corrompería la cantidad de slots ya reservados para citas existentes (decisión de ingeniería en DEC-009, no pedida explícitamente por la HU).

## Errores

`application/problem+json`, consistente con el resto de la API: `400` validación o referencia inválida (`regimeId` inexistente, duración fuera de `{30, 60}`), `403` rol distinto de ADMIN, `404` recurso o padre inexistente, `409` código duplicado.
