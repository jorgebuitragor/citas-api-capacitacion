---
id: HU-021
tipo: historia-de-usuario
titulo: "Decidir reprogramación"
estado: Completada
epica: "[[EP-006-gestion-de-citas-del-usuario]]"
esfuerzo: Muy alto
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-020-solicitar-reprogramacion]]"]
relacionadas: ["[[HU-024-consultar-bandeja-administrativa]]", "[[HU-025-consultar-auditoria-de-estados]]"]
---
# HU-021 — Decidir reprogramación
## Historia de usuario
**COMO** ADMIN **QUIERO** aprobar o rechazar una reprogramación pendiente **PARA** sustituir la cita solo cuando corresponda o conservar la original.
## Alcance
- Aprobación: libera original, asigna nueva y actualiza cita. Rechazo: libera nueva y mantiene original; motivo cuando corresponda.
## Fuera de alcance
- Cancelación automática de la original tras rechazo.
## Reglas de negocio
- RN-09, RN-10, RN-11; cambios atómicos y auditables.
## Dependencias y relaciones
- Épica: [[EP-006-gestion-de-citas-del-usuario]]; depende de [[HU-020-solicitar-reprogramacion]].
## Esfuerzo
**Nivel:** Muy alto. **Justificación:** transición multirreserva y consistencia transaccional; revisar subdivisión técnica.
## Tareas de desarrollo
- [ ] **T-01 — Definir decisiones/transiciones.** Dificultad: Alto. Validar estado/actor/motivo.
- [ ] **T-02 — Aplicar sustitución o liberación atómica.** Dificultad: Alto. Pruebas de fallo y concurrencia.
- [ ] **T-03 — Integrar bandeja de decisión.** Dificultad: Medio. Motivo y feedback.
## Criterios de aceptación
### CA-01 — Aprobación sustituye
**Dado** una reprogramación `PENDING` válida **Cuando** ADMIN aprueba **Entonces** se liberan slots originales, se asignan los nuevos y la cita queda actualizada.
### CA-02 — Rechazo conserva
**Dado** una reprogramación `PENDING` **Cuando** ADMIN rechaza según motivo aplicable **Entonces** libera la nueva franja y mantiene intacta la cita original.
### CA-03 — Auditoría
**Dado** cualquier decisión válida **Cuando** se inspecciona el historial **Entonces** cada transición relevante conserva actor, fuente, fecha/hora y motivo aplicable.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Consistencia transaccional, autorizaciones, auditoría, pruebas integración y contrato verificadas.
- [x] La HU se divide si no puede eliminar riesgo Muy alto con un diseño aprobado.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `BookingIntegrationTest.adminApprovalSwapsSlotsAtomicallyAndKeepsAudit`; `BookingService.decideReschedule`; `RescheduleInbox.test.tsx` «approves without a reason» | En una sola transacción libera los slots originales, confirma la retención como reserva firme y mueve la franja de la cita; decidir dos veces responde `409` sin mutar. |
| CA-02 | Cumple | `BookingIntegrationTest.adminRejectionRequiresReasonReleasesHoldAndKeepsOriginalAppointment`; `RescheduleInbox.test.tsx` «blocks a rejection without reason» | Rechazo sin motivo `400` sin cambios; con motivo libera la retención y la cita original conserva franja, slots y estado `APPROVED`. |
| CA-03 | Cumple | `BookingIntegrationTest.adminApprovalSwapsSlotsAtomicallyAndKeepsAudit`; `BookingIntegrationTest.adminRejectionRequiresReasonReleasesHoldAndKeepsOriginalAppointment` | Cada decisión inserta un evento `appointment_status_history` con actor ADMIN, fuente `ADMIN`, fecha/hora y motivo aplicable, y persiste decisor, fecha y motivo en la solicitud. |
| DoD | Cumple | [Contrato aprobado](../../../contracts/appointments.md); `BookingIntegrationTest.rescheduleInboxAndDecisionRequireAdminRoleAndRequestRequiresUserRole`; [evidencia S4](../../../evidence/goals-loops/S4/HU-020-HU-021-preflight-checkpoint.md) | Bloqueo de solicitud y cita en la misma transacción, autorización ADMIN verificada, API 25/25 y frontend typecheck + 23 Vitest + build. Subdivisión evaluada y descartada en DEC-005. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-29 — Aprobada explícitamente por instrucción del usuario. Criterios y DoD se conservan sin cambios.
- 2026-09-29 — Completada con implementación cross-repo, contrato aprobado, migración V7 y verificaciones API/frontend.
## Notas y decisiones
- “Motivo cuando corresponda” requiere definir obligatoriedad exacta para rechazo de reprogramación.
- Resuelto en DEC-005: el rechazo exige motivo no vacío y la aprobación admite motivo opcional que se audita si se envía. La HU se mantiene íntegra, sin subdivisión.
