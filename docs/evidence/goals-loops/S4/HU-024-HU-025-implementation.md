# S4 — Evidencia de implementación HU-024 / HU-025 (bandeja administrativa y auditoría de estados)

- **Fecha:** 2026-10-02.
- **Resultado:** **IMPLEMENTADO Y VERIFICADO** tras la aprobación explícita del usuario en chat (DEC-007 en `docs/wiki/llm-wiki/wiki/decisiones.md`), que habilitó GOAL S4.04.
- **Repositorios modificados:** `citas-api` y `citas-web`, ambos en `develop`.

## Decisión que habilitó la implementación

- **DEC-007** (`decisiones.md`): el usuario aprobó HU-024 sin cambios y ratificó la matriz de visibilidad de auditoría por **ownership** ya borrador en `docs/contracts/appointments.md:398` (ADMIN sobre cualquier cita, USER sobre sus propias citas, PROFESSIONAL sobre las que atendió), descartando la alternativa de restringir el endpoint solo a ADMIN que se evaluó primero en la misma conversación.
- La actualización formal del campo `estado` de `docs/wiki/scrum/historias-de-usuario/HU-024-*.md` y `HU-025-*.md` a `Aprobada`/`Completada` queda pendiente de una ejecución de `scrum-spec-orchestrator`, único responsable de escribir en `docs/wiki/scrum/` según `AGENTS.md`. Este documento y DEC-007 son la evidencia de que la aprobación de negocio ocurrió.

## Contrato REST

`docs/contracts/appointments.md`, sección "HU-024 / HU-025 — bandeja administrativa y auditoría de estados", actualizada de "PROPUESTA — pendiente de aprobación" a "aprobado — DEC-007":

- `GET /api/v1/admin/appointments?status=REQUESTED&locationId&professionalId&specialtyId&date` — ADMIN.
- `GET /api/v1/admin/reschedule-requests?status=PENDING&locationId&professionalId&specialtyId&date` — ADMIN.
- `GET /api/v1/appointments/{appointmentId}/status-history` — ADMIN, USER propietario, PROFESSIONAL de la cita.

## Backend (`citas-api`)

- `BookingPorts`: `findPending(...)` y `findRescheduleRequests(...)` ganan filtros `locationId/professionalId/specialtyId/date`; se agregan `findAppointmentOwnership(...)` y `findStatusHistory(...)`, además de los registros `AppointmentOwnership` y `StatusHistoryEvent`.
- `BookingCommands`: nuevo `StatusHistoryQuery(principalUserId, admin, appointmentId)`.
- `BookingService`: `pending(...)`/`rescheduleRequests(...)` pasan los filtros al puerto sin lógica adicional (son consultas ADMIN ya protegidas por Spring Security); `statusHistory(...)` resuelve ownership (admin, o `patientUserId` igual al principal, o `findProfessionalId` igual al profesional de la cita) y responde `NOT_FOUND` (404) sin distinguir "no existe" de "no autorizado", igual que el patrón ya usado en cancelación/cierre.
- `BookingPersistenceAdapter`: las dos consultas de bandeja ahora arman el `WHERE` con `StringBuilder` igual que `findMyAppointments`/`findAgenda`; `findAppointmentOwnership` es una consulta mínima sin bloqueo (no es una operación de escritura); `findStatusHistory` hace `LEFT JOIN` a `users` para que el actor sea `null` cuando la fuente es `SYSTEM`.
- `BookingController`: filtros `@RequestParam(required = false) @Positive Long` en los dos endpoints de bandeja; nuevo `GET /appointments/{id}/status-history` que lee `Authentication` para derivar `admin` (`ROLE_ADMIN` en las autoridades) y el `principalUserId`.
- `SecurityConfiguration`: nuevo matcher `/api/v1/appointments/*/status-history` con `hasAnyRole("ADMIN", "USER", "PROFESSIONAL")`, evaluado antes del matcher genérico `USER` de `/api/v1/appointments/**` (mismo patrón que `/api/v1/appointments/*/closure`).
- Sin migración nueva: `appointment_status_history` ya tenía todas las columnas necesarias desde V3 (DEC-001).

### Pruebas backend

`BookingIntegrationTest` — 4 pruebas nuevas:

- `adminInboxFiltersRequestedAppointmentsAndPendingReschedulesByLocationProfessionalSpecialtyAndDate`
- `adminInboxRequiresAdminRoleAndRejectsNonPositiveFilters`
- `statusHistoryVisibleToAdminOwnerUserAndOwnerProfessionalButDeniedToOthers` (incluye ADMIN sobre cualquier cita, USER ajeno y PROFESSIONAL ajeno → `404`, y cita inexistente → `404`)
- `statusHistoryHasNoCrudEndpoints` (`POST` al mismo path → `405`, confirmando que no hay CRUD de auditoría)

**Ejecución real en contenedor** (`docker compose exec citas-api-dev mvn -o test`):

```
Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
```

Las 29 incluyen las 25 preexistentes (S3, HU-018–021, HU-022/023) sin regresiones, más las 4 nuevas.

## Frontend (`citas-web`)

- `src/api/booking.ts`: tipos `StatusHistoryEvent`, `AdminInboxFilters`; `bookingApi.pending(...)` y `bookingApi.rescheduleRequests(...)` aceptan filtros opcionales; nuevo `bookingApi.statusHistory(...)`.
- `src/components/StatusHistory.tsx` (nuevo): `StatusHistoryToggle`, componente compartido que expande/colapsa el historial de una cita bajo demanda (sin refetch al volver a expandir), con estados `loading`/`empty`/`error`/éxito. Reutilizado sin cambios en las tres pantallas por rol, consistente con la matriz de ownership de DEC-007.
- `src/App.tsx`: `InboxFilterBar` (sede/especialidad/fecha; el backend también admite `professionalId`, omitido en la UI para mantenerla simple) agregada a `AdminDashboard` y `RescheduleInbox`; `StatusHistoryToggle` agregado a cada tarjeta de ambas bandejas.
- `src/components/MyAppointments.tsx`: `StatusHistoryToggle` agregado a `AppointmentCard` (vista de lista; la vista de calendario no se modificó).
- `src/components/ProfessionalAgenda.tsx`: `StatusHistoryToggle` agregado a `AgendaCard`.

### Pruebas frontend

- `src/components/StatusHistory.test.tsx` (nuevo) — 6 pruebas: colapsado por defecto sin fetch, eventos con actor, evento `SYSTEM` sin actor, estado vacío, estado de error (404 propagado desde la API), no repite el fetch al volver a expandir.
- `src/RescheduleInbox.test.tsx`: los mocks de `fetch` se actualizaron (`mockFetch`) para responder `[]` a las nuevas llamadas de catálogos que dispara `InboxFilterBar`, sin cambiar el comportamiento verificado por las 5 pruebas existentes.

**Ejecución real en contenedor** (`docker compose exec citas-web-dev npm run test`):

```
Test Files  5 passed (5)
     Tests  29 passed (29)
```

`npm run typecheck` y `npm run build` — ambos limpios, sin errores.

## Verificación manual end-to-end (navegador real contra backend y MySQL reales)

Ejecutada contra el entorno Docker (`fcv-citas-api-dev`, `fcv-citas-mysql`, `fcv-citas-web-dev`), con datos sintéticos temporales creados y eliminados en esta misma sesión:

1. Se creó una cuenta de prueba (`admin.lab@example.test`) desde el formulario de registro real de la UI y se le otorgó el rol `ADMIN` directamente en MySQL (la cuenta conservó también `USER`, lo que permitió probar ambos roles con la misma sesión).
2. Como `USER`, se solicitó una cita de Ortopedia y Traumatología (especialidad que requiere aprobación ADMIN) desde `/booking`. Apareció `REQUESTED`.
3. Como `ADMIN`, en "Bandeja ADMIN" la solicitud apareció en "Solicitudes de cita especializada" con los filtros de sede/especialidad/fecha visibles. Filtrar por una especialidad distinta la ocultó (`No hay solicitudes especializadas pendientes.`); limpiar el filtro la mostró de nuevo.
4. "Ver historial" sobre esa misma tarjeta mostró el evento `Solicitada · Usuario (Admin Laboratorio) · ... — Solicitud especializada creada`.
5. Se aprobó la solicitud desde la bandeja. En "Mis citas" (rol `USER` de la misma cuenta), la cita apareció `Aprobada`, con "Ver historial" visible y mostrando ambos eventos (`Solicitada` y `Aprobada`, con actor y motivo correctos) — confirma ownership de `USER` sobre su propia cita.
6. Se solicitó una reprogramación desde "Mis citas". En "Reprogramaciones pendientes" de la Bandeja ADMIN apareció con sus propios filtros; "Ver historial" sobre esa tarjeta (que usa `item.appointmentId`, no el id de la reprogramación) mostró los tres eventos acumulados de la misma cita, incluido `Reprogramación solicitada para 2026-10-03T10:00`.
7. **Limpieza:** se eliminó la cita y su historial, la solicitud de reprogramación, los `refresh_tokens` y el usuario de prueba; no quedan datos de esta verificación en el entorno compartido.

No se verificó manualmente el toggle de historial en la vista de `PROFESSIONAL` (agenda) por no contar con una contraseña real conocida para las cuentas semilla `prof.*@demo.invalid` (están `active = FALSE` y su hash no es sintético-reversible); queda cubierto por `statusHistoryVisibleToAdminOwnerUserAndOwnerProfessionalButDeniedToOthers` a nivel de integración y por revisión de código, ya que `ProfessionalAgenda.tsx` reutiliza el mismo componente `StatusHistoryToggle` ya probado en las otras dos pantallas.

## Criterios de aceptación — estado

| Criterio | Evidencia | Estado |
|---|---|---|
| HU-024 CA-01 (pendientes correctos: `REQUESTED` + `PENDING`) | `adminInboxFiltersRequestedAppointmentsAndPendingReschedulesByLocationProfessionalSpecialtyAndDate`; verificación manual pasos 2–3 | Cumplido |
| HU-024 CA-02 (filtros se aplican) | Mismo test (filtro que excluye y filtro que incluye); verificación manual paso 3 | Cumplido |
| HU-024 CA-03 (rol ADMIN exigido) | `adminInboxRequiresAdminRoleAndRejectsNonPositiveFilters` | Cumplido |
| HU-025 CA-01 (evento completo: estado, actor, fuente, fecha, motivo) | `statusHistoryVisibleToAdminOwnerUserAndOwnerProfessionalButDeniedToOthers`; verificación manual pasos 4 y 6 | Cumplido |
| HU-025 CA-02 (inmutabilidad: sin CRUD) | `statusHistoryHasNoCrudEndpoints` | Cumplido |
| HU-025 CA-03 (visibilidad controlada por ownership) | Mismo test (USER y PROFESSIONAL ajenos → `404`); verificación manual paso 5 (USER propietario) | Cumplido |
| UI con estados `loading`/`empty`/`error`/éxito | `StatusHistory.test.tsx`; `InboxFilterBar` reutiliza los estados ya existentes de `AdminDashboard`/`RescheduleInbox` | Cumplido |
| Sin paginación, orden o permisos no aprobados | No se agregó paginación ni parámetros fuera del contrato aprobado | Cumplido |

## Resultado

- **PASADO.** 29/29 pruebas backend y 29/29 pruebas frontend en verde, typecheck y build limpios, y verificación manual end-to-end exitosa contra el backend y MySQL reales para los roles ADMIN y USER.
- Pendiente para cerrar formalmente el ciclo Scrum: ejecución de `scrum-spec-orchestrator` para marcar HU-024/HU-025 `Completada` con CA/DoD y trazabilidad, ya que solo esa skill escribe en `docs/wiki/scrum/`.
- No verificado manualmente en navegador: rol `PROFESSIONAL` sobre `status-history` (ver nota arriba); filtro `professionalId` de la bandeja (disponible en la API, sin control en esta UI).
