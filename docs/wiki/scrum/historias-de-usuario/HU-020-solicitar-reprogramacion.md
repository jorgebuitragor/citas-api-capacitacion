---
id: HU-020
tipo: historia-de-usuario
titulo: "Solicitar reprogramación"
estado: Completada
epica: "[[EP-006-gestion-de-citas-del-usuario]]"
esfuerzo: Muy alto
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-018-consultar-mis-citas]]", "[[HU-014-consultar-disponibilidad-para-reserva]]"]
relacionadas: ["[[HU-021-decidir-reprogramacion]]"]
---
# HU-020 — Solicitar reprogramación
## Historia de usuario
**COMO** USER **QUIERO** proponer una nueva fecha/hora disponible para una cita aprobada futura **PARA** cambiarla sin perder mi cita original mientras ADMIN decide.
## Alcance
- Solicitud `PENDING`, conserva profesional/especialidad, retiene nueva franja y conserva original.
## Fuera de alcance
- Cambiar profesional; se trata como nueva cita.
## Reglas de negocio
- RN-01, RN-05, RN-10; solo aprobada/futura; nueva franja completa y original intacta.
## Dependencias y relaciones
- Épica: [[EP-006-gestion-de-citas-del-usuario]]; depende de [[HU-018-consultar-mis-citas]], [[HU-014-consultar-disponibilidad-para-reserva]].
## Esfuerzo
**Nivel:** Muy alto. **Justificación:** mantiene dos franjas, estado, concurrencia y auditoría; debe revisarse para dividir diseño/modelo y flujo si crece.
## Tareas de desarrollo
- [ ] **T-01 — Modelar solicitud y retención provisional.** Dificultad: Alto. Migración 3FN, relación a cita original/nueva franja.
- [ ] **T-02 — Crear solicitud atómica.** Dificultad: Alto. Validar elegibilidad, profesional/especialidad y slots.
- [ ] **T-03 — Integrar flujo de selección.** Dificultad: Alto. UX aprobada con estados y conflicto.
## Criterios de aceptación
### CA-01 — Solicitud elegible
**Dado** una cita propia `APPROVED` y futura **Cuando** USER selecciona nueva franja válida con mismo profesional/especialidad **Entonces** se crea reprogramación `PENDING` y se retiene la nueva franja.
### CA-02 — Original conservada
**Dado** una reprogramación pendiente **Cuando** se consulta la cita original **Entonces** conserva su franja hasta una aprobación ADMIN.
### CA-03 — Restricciones
**Dado** una cita no elegible, cambio de profesional o franja incompleta/conflictiva **Cuando** se solicita reprogramación **Entonces** no se crea solicitud ni retención parcial.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Migración, modelo 3FN, concurrencia, ownership, pruebas integración y contrato verificadas.
- [x] La división de esta HU se reconsideró si el diseño aprobado mantiene complejidad muy alta.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `BookingIntegrationTest.userRequestsReschedulePendingHoldsNewSlotAndKeepsOriginal`; `BookingService.requestReschedule`; `src/components/MyAppointments.tsx` (`RescheduleDialog`) | `POST /api/v1/appointments/{id}/reschedule-requests` crea la solicitud `PENDING` y retiene la nueva franja con `professional_slots.reschedule_request_id`. |
| CA-02 | Cumple | `BookingIntegrationTest.userRequestsReschedulePendingHoldsNewSlotAndKeepsOriginal`; `MyAppointments.test.tsx` «shows the pending reschedule without losing the original slot» | La cita conserva `scheduled_start_at`, sus slots y su estado `APPROVED`; la tarjeta muestra la franja original y el bloque de pendiente. |
| CA-03 | Cumple | `BookingIntegrationTest.rescheduleRequestRejectsForeignProfessionalChangeConflictAndIneligibleAppointment`; `BookingIntegrationTest.heldRescheduleSlotIsNotOfferedNorReservable` | Cita ajena `404`, cambio de profesional/especialidad `400`, franja ocupada o solicitud duplicada `409`, cita `REQUESTED` `409`; ningún caso crea solicitud ni retención parcial. |
| DoD | Cumple | `V7__add_reschedule_requests.sql`; [contrato aprobado](../../../contracts/appointments.md); [evidencia S4](../../../evidence/goals-loops/S4/HU-020-HU-021-preflight-checkpoint.md) | Migración idempotente en 3FN, bloqueo pesimista de DEC-003, ownership desde JWT, API 25/25 y frontend typecheck + 23 Vitest + build. Subdivisión evaluada y descartada en DEC-005. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-29 — Aprobada explícitamente por instrucción del usuario. Criterios y DoD se conservan sin cambios.
- 2026-09-29 — Completada con implementación cross-repo, contrato aprobado, migración V7 y verificaciones API/frontend.
## Notas y decisiones
- Candidata a subdivisión tras acordar modelo/retención; no se implementa hasta resolverlo.
- Resuelto en DEC-005: la HU se mantiene íntegra, sin subdivisión, y se implementa junto con HU-021 como un único incremento cross-repo. La retención usa la columna nueva `professional_slots.reschedule_request_id` con el bloqueo pesimista aprobado en DEC-003.
