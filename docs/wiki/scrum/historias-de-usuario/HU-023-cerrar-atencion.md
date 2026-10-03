---
id: HU-023
tipo: historia-de-usuario
titulo: "Cerrar atención"
estado: Completada
epica: "[[EP-007-operacion-clinica-del-profesional]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-022-consultar-agenda-profesional]]"]
relacionadas: ["[[HU-025-consultar-auditoria-de-estados]]"]
---
# HU-023 — Cerrar atención
## Historia de usuario
**COMO** PROFESSIONAL **QUIERO** marcar una cita propia pasada/aplicable como `COMPLETED` o `NO_SHOW` **PARA** reflejar su resultado operativo.
## Alcance
- Transiciones permitidas y auditoría.
## Fuera de alcance
- Diagnósticos, tratamientos o cambios de citas ajenas.
## Reglas de negocio
- Solo profesional propio; cita pasada/aplicable; historial obligatorio.
## Dependencias y relaciones
- Épica: [[EP-007-operacion-clinica-del-profesional]]; depende de [[HU-022-consultar-agenda-profesional]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** ownership y transición con criterio temporal pendiente.
## Tareas de desarrollo
- [x] **T-01 — Definir elegibilidad/transición.** Dificultad: Medio. Acordar “aplicable”.
- [x] **T-02 — Registrar resultado/auditoría.** Dificultad: Medio. Prohibir edición CRUD.
- [x] **T-03 — Integrar acción de agenda.** Dificultad: Bajo. Estados y confirmación.
## Criterios de aceptación
### CA-01 — Cierre propio válido
**Dado** una cita propia aprobada pasada/aplicable **Cuando** PROFESSIONAL la marca **Entonces** queda `COMPLETED` o `NO_SHOW` y se audita.
### CA-02 — Cierre inválido
**Dado** una cita futura, ajena o no elegible **Cuando** intento cerrarla **Entonces** no cambia.
## Definition of Done
- [x] CA-01 y CA-02 validados con evidencia.
- [x] Ownership, transición/auditoría, pruebas y contrato aplicables verificados.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `BookingIntegrationTest.professionalCanCloseEligiblePastApprovedAppointmentAndRecordsAudit` | PROFESSIONAL marca una cita propia `APPROVED` y ya finalizada como `COMPLETED` o `NO_SHOW`; cada transición queda auditada con actor, fuente `USER` y fecha/hora del servidor. |
| CA-02 | Cumple | `BookingIntegrationTest.cannotCloseFutureOrForeignAppointment` | Cita futura, ajena o ya terminal no cambia (`409`/`404`); ninguna respuesta de error muta datos. |
| DoD | Cumple | `docs/contracts/appointments.md`; backend 25/25 (en la sesión original); `docs/evidence/goals-loops/S4/HU-022-HU-023-implementation.md` | Verificación manual en MySQL real confirmó `appointment_status_history` con auditoría explícita. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-29 — DEC-006: el usuario fija "pasada/aplicable" = `endAt <= ahora` en `America/Bogota`, sin tolerancia, igual para 30 y 60 minutos, y aprueba el inicio de implementación.
- 2026-09-29 — Implementada y verificada (backend, frontend y manual end-to-end contra MySQL real); evidencia en `docs/evidence/goals-loops/S4/HU-022-HU-023-implementation.md`.
- 2026-10-02 — El usuario instruye marcar formalmente `Completada`; se actualiza este campo y la tabla de evidencia, que habían quedado pendientes de una ejecución de `scrum-spec-orchestrator`.
## Notas y decisiones
- "Pasada/aplicable" resuelta en DEC-006: `endAt <= ahora` en `America/Bogota`, sin tolerancia, igual para 30 y 60 minutos.
