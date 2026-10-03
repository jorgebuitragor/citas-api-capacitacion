# S4 — Evidencia de implementación HU-006 / HU-007 / HU-008 (catálogos configurables)

- **Fecha:** 2026-10-03.
- **Resultado:** **IMPLEMENTADO Y VERIFICADO** tras la aprobación explícita del usuario en chat (DEC-009 en `docs/wiki/llm-wiki/wiki/decisiones.md`), que habilitó GOAL S4.06 — el último goal pendiente de S4.
- **Repositorios modificados:** `citas-api` y `citas-web`, ambos en `develop`.

## Decisión que habilitó la implementación

- **DEC-009** (`decisiones.md`): la pregunta de unicidad de HU-006 ("unicidad nominal/código se define en contrato/modelo") se resolvió **sin preguntar al usuario**, siguiendo la autoridad ya fijada en DEC-001 (`database/reference/db.sql`): `eps.code` es único (no el nombre); `eps_plans` es único por `(eps_id, code)`. El usuario aprobó el plan completo: traer `insurance_regimes`/`eps`/`eps_plans` tal cual el esquema de referencia, CRUD de EPS/planes/especialidades solo para ADMIN, desactivación en vez de borrado físico, y `code` (más `durationMinutes`/`general`/`requiresAdminApproval` en especialidades) inmutables tras crear — decisión de ingeniería para no corromper citas ya reservadas, no pedida explícitamente por la HU.

## Contrato REST

Nuevo archivo `docs/contracts/catalogs.md`:

- `GET /api/v1/catalogs/insurance-regimes` — USER.
- `GET/POST /api/v1/admin/catalogs/eps`, `PATCH /api/v1/admin/catalogs/eps/{id}` — ADMIN.
- `GET/POST /api/v1/admin/catalogs/eps/{epsId}/plans`, `PATCH .../plans/{planId}` — ADMIN.
- `GET/POST /api/v1/admin/catalogs/specialties`, `PATCH /api/v1/admin/catalogs/specialties/{id}` — ADMIN.

## Backend (`citas-api`)

- Migración `V9__add_insurance_regimes_eps_and_plans.sql`: tablas `insurance_regimes`, `eps`, `eps_plans` tomadas tal cual del esquema de referencia, con el mismo seed de demostración (3 EPS, 5 planes, 5 regímenes). `specialties` ya existía desde V3 y no se modificó.
- Nuevo módulo `application/catalog` (puerto, comandos, excepción, servicio) siguiendo el mismo patrón hexagonal que `application/booking`: `CatalogService` valida duración (solo 30/60), código/nombre no vacíos, duplicados (`409`) y referencias (EPS/régimen inexistente → `404`/`400`); nunca borra, solo alterna `active`.
- Nuevo `CatalogPersistenceAdapter` (JDBC crudo, mismo estilo que `BookingPersistenceAdapter`) y `CatalogController` (nuevo), reutilizando sin cambios los matchers de seguridad ya existentes (`/api/v1/admin/**` → ADMIN, `/api/v1/catalogs/**` → USER).
- `ApiExceptionHandler`: nuevo `@ExceptionHandler(CatalogException.class)` (`400`/`404`/`409`).

### Pruebas backend

`CatalogIntegrationTest` (nuevo) — 8 pruebas: EPS (crear/listar incluyendo inactivas/código duplicado/desactivar), rol ADMIN exigido, planes (crear bajo EPS válida, EPS inexistente → `404`, régimen inexistente → `400`, código duplicado → `409`, desactivar), rol ADMIN exigido en planes, especialidades (duración inválida → `400`, crear con 60 min, código duplicado → `409`, `code`/`durationMinutes` inmutables tras `PATCH`), rol ADMIN y lista incluye inactivas, régimen público para cualquier USER autenticado.

**Ejecución real en contenedor** (`docker compose exec citas-api-dev mvn -o test`):

```
Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
```

Las 41 incluyen las 33 preexistentes (S3, HU-004, HU-018–025) sin regresiones, más las 8 nuevas.

## Frontend (`citas-web`)

- `src/api/catalog.ts` (nuevo): tipos y funciones para los cuatro recursos.
- `src/components/CatalogAdmin.tsx` (nuevo): `CatalogsDashboard` con tres secciones — EPS (lista + crear + activar/desactivar), planes anidados por EPS seleccionada (lista + crear con selector de régimen + activar/desactivar), especialidades (lista + crear con duración 30/60 + checkboxes `general`/`requiresAdminApproval` + activar/desactivar). Reutiliza `Field`/`NoticeBox`/`PortalLayout` ya aprobados (se exportaron desde `App.tsx`, sin cambiar su implementación).
- `src/App.tsx`: nueva ruta `catalogs`, botón "Catálogos" en el header de `AdminDashboard`.

### Pruebas frontend

`src/components/CatalogAdmin.test.tsx` (nuevo) — 5 pruebas: listado inicial, creación de EPS y recarga, desactivación de EPS, planes de una EPS (vacío → crear → confirmación), error de servidor mostrado al rechazar una duración inválida.

**Ejecución real en contenedor** (`docker compose exec citas-web-dev npm run test`):

```
Test Files  7 passed (7)
     Tests  39 passed (39)
```

`npm run typecheck` y `npm run build` — ambos limpios, sin errores.

## Verificación manual end-to-end (navegador real contra backend y MySQL reales)

1. Se creó una cuenta de prueba (`carla.catalogos@example.test`) desde el registro real y se promovió a `ADMIN` por SQL.
2. En "Bandeja ADMIN" apareció el nuevo botón "Catálogos"; la pantalla cargó correctamente las 3 EPS, 5 planes y 3 especialidades sembradas por la migración.
3. **Bug real encontrado y corregido durante la verificación:** el primer intento de desactivar un plan falló — el preflight CORS (`OPTIONS`) respondía `403` para `PATCH`. `SecurityConfiguration.corsConfigurationSource` solo permitía `GET, POST, OPTIONS`; `PATCH` nunca se agregó al introducir los endpoints de actualización de HU-024 (`admin/appointments`, que en realidad usa `POST .../decision`, no `PATCH`) — este contrato es el primero del proyecto en usar `PATCH`. Se corrigió agregando `PATCH` a la lista y se agregó una prueba de regresión (`corsAllowsPatchForConfiguredOrigin`) que ninguna prueba anterior cubría, porque `MockMvc` sin encabezado `Origin` no dispara el filtro CORS — solo un preflight real (navegador o `OPTIONS` explícito con `Access-Control-Request-Method`) lo detecta.
4. Tras la corrección y reinicio del backend: se creó un plan nuevo bajo "Atención Particular Demo" (régimen Contributivo) → apareció en la lista; se desactivó → `PATCH` respondió `200` y la UI mostró "Inactivo"/"Activar".
5. Se creó una especialidad nueva ("Dermatología (prueba)", 60 min) → apareció en la lista con duración correcta; se desactivó → `PATCH` respondió `200`.
6. **Limpieza:** se eliminaron el plan y la especialidad de prueba, y la cuenta de prueba (rol, refresh tokens y usuario) de MySQL real. Los 3 EPS, 5 planes y 3 especialidades originales del seed quedaron intactos.

## Criterios de aceptación — estado

| Criterio | Evidencia | Estado |
|---|---|---|
| HU-006 CA-01/CA-03 (administración autorizada, sin borrado físico) | `CatalogIntegrationTest.createsEpsListsItIncludingInactiveAndRejectsDuplicateCode`; verificación manual pasos 2, 4 | Cumplido |
| HU-006 CA-02 (protección de referencias) | No existe endpoint de borrado; solo `active` se expone en el contrato | Cumplido |
| HU-006 CA-03 / rol | `epsManagementRequiresAdminRole` | Cumplido |
| HU-007 CA-01 (plan asociado a su EPS) | `createsPlanUnderExistingEpsAndValidatesEpsAndRegimeReferences`; verificación manual paso 4 | Cumplido |
| HU-007 CA-03 (catálogo en uso no se borra) | No existe endpoint de borrado para planes | Cumplido |
| HU-008 CA-01 (duración válida 30/60) | `createsSpecialtyValidatesDurationAndKeepsCodeAndDurationImmutable` | Cumplido |
| HU-008 CA-03 (no se borra, se desactiva) | No existe endpoint de borrado para especialidades; verificación manual paso 5 | Cumplido |
| Rol ADMIN en los tres recursos | `plansManagementRequiresAdminRole`, `specialtiesManagementRequiresAdminRoleAndListIncludesInactive` | Cumplido |

## Resultado

- **PASADO.** 41/41 pruebas backend y 39/39 pruebas frontend en verde, typecheck y build limpios, y verificación manual end-to-end exitosa contra el backend y MySQL reales — incluido un bug real de CORS detectado y corregido durante esa verificación, con su propia prueba de regresión.
- Con esto, **los seis GOAL de S4 quedan implementados y verificados.**
- Pendiente para cerrar formalmente el ciclo Scrum: ejecución de `scrum-spec-orchestrator` para marcar HU-006/HU-007/HU-008 `Completada` con CA/DoD y trazabilidad — mismo patrón que las HU anteriores, salvo que el usuario pida la actualización directa.
