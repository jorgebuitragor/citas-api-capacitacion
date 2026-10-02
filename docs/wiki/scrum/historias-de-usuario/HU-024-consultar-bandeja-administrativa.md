---
id: HU-024
tipo: historia-de-usuario
titulo: "Consultar bandeja administrativa"
estado: Completada
epica: "[[EP-008-operacion-administrativa-y-trazabilidad]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 5"
dependencias: ["[[HU-016-solicitar-cita-especializada]]", "[[HU-020-solicitar-reprogramacion]]"]
relacionadas: ["[[HU-017-decidir-cita-especializada]]", "[[HU-021-decidir-reprogramacion]]"]
---
# HU-024 — Consultar bandeja administrativa
## Historia de usuario
**COMO** ADMIN **QUIERO** ver citas especializadas `REQUESTED` y reprogramaciones `PENDING` filtradas **PARA** resolver las solicitudes pendientes.
## Alcance
- Bandeja y filtros sede, profesional, especialidad y fecha.
## Fuera de alcance
- Decidir una solicitud desde esta HU.
## Reglas de negocio
- Solo ADMIN; estados exactos de pendientes.
## Dependencias y relaciones
- Épica: [[EP-008-operacion-administrativa-y-trazabilidad]]; depende de [[HU-016-solicitar-cita-especializada]], [[HU-020-solicitar-reprogramacion]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** proyección de dos tipos de solicitud y filtros.
## Tareas de desarrollo
- [x] **T-01 — Diseñar consultas de bandeja.** Dificultad: Medio. Filtros e índices.
- [x] **T-02 — Proteger acceso ADMIN.** Dificultad: Medio. Pruebas de rol.
- [x] **T-03 — Renderizar pendientes.** Dificultad: Medio. Estados y enlaces de decisión.
## Criterios de aceptación
### CA-01 — Pendientes correctos
**Dado** solicitudes existentes **Cuando** ADMIN abre la bandeja **Entonces** ve especializadas `REQUESTED` y reprogramaciones `PENDING`.
### CA-02 — Filtros
**Dado** solicitudes variadas **Cuando** aplica los filtros exigidos **Entonces** el resultado se ajusta a ellos.
### CA-03 — Rol
**Dado** un rol no ADMIN **Cuando** accede a la bandeja **Entonces** el servidor lo deniega.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Filtros, autorización, contrato, pruebas y estados web aplicables verificados.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `BookingIntegrationTest.adminInboxFiltersRequestedAppointmentsAndPendingReschedulesByLocationProfessionalSpecialtyAndDate` | La bandeja proyecta solicitudes especializadas `REQUESTED` y reprogramaciones `PENDING` reutilizando `GET /api/v1/admin/appointments` y `GET /api/v1/admin/reschedule-requests`. |
| CA-02 | Cumple | Mismo test (filtro que excluye y filtro que incluye por sede/especialidad/profesional/fecha); verificación manual en navegador real | Filtros combinables con `AND`, todos opcionales, aplicados a nivel SQL en `BookingPersistenceAdapter`. |
| CA-03 | Cumple | `BookingIntegrationTest.adminInboxRequiresAdminRoleAndRejectsNonPositiveFilters` | `/api/v1/admin/**` exige `hasRole("ADMIN")`; rol no ADMIN y sin autenticación responden `403`/`401`. |
| DoD | Cumple | `docs/contracts/appointments.md`; backend 29/29; frontend typecheck, Vitest 29/29 y build; `docs/evidence/goals-loops/S4/HU-024-HU-025-implementation.md` | Estados loading/empty/error/success implementados en `InboxFilterBar`; contrato marcado "aprobado — DEC-007". Sin cambio de esquema. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-10-02 — DEC-007: el usuario aprueba la HU sin cambios sobre su alcance y CA.
- 2026-10-02 — Validada y completada con evidencia de implementación y verificaciones backend/frontend registradas en `docs/evidence/goals-loops/S4/HU-024-HU-025-implementation.md`.
## Notas y decisiones
- Orden/paginación queda para el contrato.
- El filtro `professionalId` existe en la API (contrato y pruebas) pero no se expuso como control en la UI de esta iteración; queda disponible para una iteración futura.
