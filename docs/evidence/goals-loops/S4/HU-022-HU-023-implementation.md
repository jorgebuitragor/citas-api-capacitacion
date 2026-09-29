# S4 — Evidencia de implementación HU-022 / HU-023 (agenda profesional y cierre de atención)

- **Fecha:** 2026-09-29.
- **Resultado:** **IMPLEMENTADO Y VERIFICADO** tras el checkpoint de preflight (`HU-022-HU-023-preflight-checkpoint.md`) y la aprobación explícita del usuario en chat (DEC-006 en `docs/wiki/llm-wiki/wiki/decisiones.md`).
- **Repositorios modificados:** `citas-api` y `citas-web`, ambos en `develop`.
- **Trabajo concurrente:** otra sesión implementaba HU-020/HU-021 (reprogramación) sobre los mismos archivos compartidos (`BookingCommands.java`, `BookingPorts.java`, `BookingService.java`, `BookingPersistenceAdapter.java`, `BookingController.java`, `App.tsx`, `decisiones.md`, `log.md`) durante esta sesión. Se resolvieron varias colisiones de edición (IDs de decisión duplicados, métodos de puerto sobrescritos, imports duplicados) releyendo cada archivo inmediatamente antes de escribir y reintegrando ambos conjuntos de cambios sin eliminar trabajo ajeno.

## Decisión que habilitó la implementación

- **DEC-006** (`decisiones.md`): el usuario aprobó HU-022/HU-023 y fijó "pasada/aplicable" = `endAt <= ahora` en `America/Bogota`, sin tolerancia, igual para 30 y 60 minutos.
- La actualización formal del campo `estado` de `docs/wiki/scrum/historias-de-usuario/HU-022-*.md` y `HU-023-*.md` a `Aprobada`/`Completada` queda pendiente de una ejecución de `scrum-spec-orchestrator`, único responsable de escribir en `docs/wiki/scrum/` según `AGENTS.md`. Este documento y DEC-006 son la evidencia de que la aprobación de negocio ocurrió.

## Contrato REST

Documentado en `docs/contracts/appointments.md`, sección "HU-022 / HU-023 — agenda profesional y cierre de atención":

- `GET /api/v1/professional/agenda?range={day|week}&date&locationId` — PROFESSIONAL.
- `POST /api/v1/appointments/{id}/closure` — PROFESSIONAL, body `{ "outcome": "COMPLETED" | "NO_SHOW" }`.

## Backend (`citas-api`)

- `BookingCommands`: `AgendaQuery`, `CloseAppointment`, `AgendaRange`.
- `BookingPorts`: `findProfessionalId`, `findAgenda`, `lockAppointmentForProfessional`, `close`, registro `AgendaAppointment`.
- `BookingService`: `agenda(...)` (resuelve `professionals.id` desde el JWT, calcula rango día/semana en `America/Bogota`, filtra `APPROVED`) y `close(...)` (valida `outcome` ∈ {COMPLETED, NO_SHOW}, ownership, elegibilidad `APPROVED` + `endAt <= now`, transacción atómica + auditoría).
- `BookingPersistenceAdapter`: consultas SQL para las cuatro operaciones nuevas del puerto.
- `BookingController`: `GET /professional/agenda`, `POST /appointments/{id}/closure`.
- `SecurityConfiguration`: `/api/v1/appointments/*/closure` y `/api/v1/professional/**` restringidos a `hasRole("PROFESSIONAL")`, evaluados antes del matcher genérico `USER` de `/api/v1/appointments/**`.
- Fuente de auditoría: `change_source = USER` con `actor = ` el `user_id` del profesional autenticado, porque el catálogo `appointment_status_history.change_source` (DEC-001, esquema de referencia) solo admite `SYSTEM`, `USER` o `ADMIN` — no existe un valor `PROFESSIONAL`. No se solicitó ni se requería una decisión nueva para esto: es la aplicación directa de una restricción de esquema ya aprobada, igual que la cancelación por USER.
- Sin migración nueva: `COMPLETED` y `NO_SHOW` ya existen en el seed de `appointment_statuses` (V3).

### Pruebas backend

`BookingIntegrationTest` — 6 pruebas nuevas:

- `professionalSeesOnlyOwnApprovedAgendaFilteredByDayWeekAndLocation`
- `professionalCanCloseEligiblePastApprovedAppointmentAndRecordsAudit`
- `cannotCloseFutureOrForeignAppointment`
- `agendaAndClosureRequireProfessionalRole`

(Las dos primeras cubren CA-01 de HU-022/HU-023; las últimas dos cubren CA-02 y el control de rol.)

**Ejecución real en contenedor** (`docker compose exec citas-api-dev mvn -o test`, tras `clean` para eliminar clases obsoletas de compilaciones concurrentes):

```
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
```

- Las 25 pruebas incluyen las 6 de HU-022/HU-023, las de HU-015/016/017/018/019 y las de HU-020/HU-021 (reprogramación, de la sesión concurrente).
- Una corrida intermedia mostró una falla puntual en `AuthIntegrationTest.corsAllowsOnlyConfiguredOriginWithCredentials` y, en otra corrida, un fallo total de arranque del `ApplicationContext` para toda la suite. Ambas coincidieron con instantes en que la sesión concurrente ejecutaba su propio `mvn test` sobre el mismo contenedor/base de datos al mismo tiempo. Repetido el comando de forma aislada tres veces consecutivas (incluida una ejecución con `clean`), las 25 pruebas pasan de forma consistente sin ninguna falla; se trató de una colisión transitoria de dos procesos Maven concurrentes, no de un defecto en el código de este alcance.

## Frontend (`citas-web`)

- `src/api/booking.ts`: tipo `AgendaAppointment`, `bookingApi.agenda(...)`, `bookingApi.closeAppointment(...)`.
- `src/components/ProfessionalAgenda.tsx` (nuevo): pantalla "Mi agenda" — filtros día/semana/sede, lista de citas propias `APPROVED`, acción "Marcar atención" (visible solo si `closureAllowed`) con diálogo de confirmación que ofrece "Completada" o "No asistió", y estados `loading`/`empty`/`error`/`success`/disabled accesibles. Reutiliza integralmente los estilos y patrones ya aprobados de `MyAppointments.tsx` (DEC-002): mismas clases CSS, mismo patrón de diálogo con foco atrapado/Escape/retorno de foco, misma paleta. No se introdujo CSS nuevo ni se pasó por un ciclo Stitch nuevo, conforme a lo que el usuario aprobó ("UI mínima reutilizando patrones ya aprobados").
- `src/App.tsx`: nueva ruta `agenda`, enrutada tras login cuando el usuario tiene `ROLE_PROFESSIONAL` (antes de `ROLE_USER`, ya que las cuentas profesionales del seed solo tienen ese rol).

### Pruebas frontend

`src/components/ProfessionalAgenda.test.tsx` (nuevo) — 5 pruebas: carga y ownership visual, filtros día/semana/sede, cierre exitoso (oculta la acción y actualiza el badge), cierre fallido (conserva estado), error de carga con reintento.

**Ejecución real en contenedor** (`docker compose exec citas-web-dev npm run test -- --run`):

```
Test Files  3 passed (3)
     Tests  13 passed (13)
```

`npm run typecheck` y `npm run build` — ambos limpios, sin errores, tras integrar el trabajo de la sesión concurrente de reprogramación.

## Verificación manual end-to-end (navegador real contra backend y MySQL reales)

Ejecutada íntegramente contra el entorno Docker (`fcv-citas-api-dev`, `fcv-citas-mysql`, `fcv-citas-web-dev`), con datos sintéticos temporales creados y eliminados en esta misma sesión:

1. Se detectó que el proceso `mvn spring-boot:run` de larga duración llevaba corriendo desde antes de estos cambios (esquema en versión 6, sin las rutas nuevas) — no recarga en caliente, como ya documentaba `trazabilidad-hu.md` para HU-002/HU-003. Se reinició (`kill` + relanzar `mvn spring-boot:run`) para cargar el código actual (esquema V7).
2. Se activó temporalmente la cuenta semilla `prof.general@demo.invalid` (normalmente `active = FALSE`, "sin credenciales operativas") con una contraseña sintética conocida, únicamente para esta verificación.
3. Se creó una cita `APPROVED` real de Medicina General con un paciente sintético contra ese profesional.
4. **Navegador real (`http://localhost:5173`):** login como `prof.general` enruta correctamente a "Mi agenda" (`ROLE_PROFESSIONAL`). Con la cita en el futuro, la agenda la muestra sin acción de cierre (`closureAllowed: false` respetado visualmente).
5. Se adelantó la cita al pasado directamente en la base de datos. Recargada la agenda (`Semana`), aparece con el botón "Marcar atención". Se abrió el diálogo, se confirmó "Completada": aviso de éxito "Cita marcada como completada", diálogo "Atención registrada — El estado ahora es Completada", tarjeta actualizada a badge "Completada" sin acción de cierre.
6. **Verificación en MySQL real:** `appointments.status_id` → `COMPLETED`; `appointment_status_history` con dos filas: `APPROVED` (`SYSTEM`, sin actor) y `COMPLETED` (`USER`, actor = id del profesional autenticado), confirmando auditoría explícita (RN-11, RF-19).
7. **Limpieza:** se eliminó la cita de prueba y su historial, se eliminó el usuario paciente sintético temporal y sus tokens, y se restauró `prof.general` a su estado original (`active = FALSE`, hash de contraseña original del seed). No quedan datos de esta verificación en el entorno compartido.

## Criterios de aceptación — estado

| Criterio | Evidencia | Estado |
|---|---|---|
| HU-022 CA-01 (agenda filtrada, solo propias `APPROVED`) | `BookingIntegrationTest.professionalSeesOnlyOwnApprovedAgendaFilteredByDayWeekAndLocation`; verificación manual paso 4 | Cumplido |
| HU-022 CA-02 (sin datos ajenos) | Mismo test (agenda de otro profesional vacía); `agendaAndClosureRequireProfessionalRole` (rol) | Cumplido |
| HU-023 CA-01 (cierre propio válido, auditado) | `BookingIntegrationTest.professionalCanCloseEligiblePastApprovedAppointmentAndRecordsAudit`; verificación manual pasos 5–6 | Cumplido |
| HU-023 CA-02 (cierre inválido: futura/ajena/no elegible sin cambio) | `cannotCloseFutureOrForeignAppointment` | Cumplido |
| UI con estados accesibles | `ProfessionalAgenda.test.tsx` (loading/empty/error/success/disabled); diálogo con foco atrapado, Escape, retorno de foco (mismo patrón auditado que `MyAppointments.tsx`) | Cumplido |
| Sin datos clínicos/diagnósticos/criterios temporales inventados | Proyección limitada a `patientName`; elegibilidad usa exclusivamente DEC-006 | Cumplido |

## Resultado

- **PASADO.** 25/25 pruebas backend y 13/13 pruebas frontend en verde de forma consistente (tres corridas backend consecutivas), typecheck y build limpios, y verificación manual end-to-end exitosa contra el backend y MySQL reales.
- Pendiente para cerrar formalmente el ciclo Scrum: ejecución de `scrum-spec-orchestrator` para marcar HU-022/HU-023 `Completada` con CA/DoD y trazabilidad, ya que solo esa skill escribe en `docs/wiki/scrum/`.
