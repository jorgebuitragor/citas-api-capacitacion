---
id: HU-017
tipo: historia-de-usuario
titulo: "Decidir cita especializada"
estado: Aprobada
epica: "[[EP-005-descubrimiento-y-solicitud-de-citas]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 3"
dependencias: ["[[HU-016-solicitar-cita-especializada]]"]
relacionadas: ["[[HU-024-consultar-bandeja-administrativa]]", "[[HU-025-consultar-auditoria-de-estados]]"]
---
# HU-017 — Decidir cita especializada
## Historia de usuario
**COMO** ADMIN **QUIERO** aprobar o rechazar una solicitud especializada **PARA** confirmar una atención válida o liberar oportunamente su horario.
## Alcance
- Decisión de `REQUESTED`; aprobación a `APPROVED`; rechazo a `REJECTED` con motivo y liberación.
## Fuera de alcance
- Reprogramación.
## Reglas de negocio
- RN-03, RN-04, RN-09, RN-11; solo ADMIN decide y cambio queda auditado.
## Dependencias y relaciones
- Épica: [[EP-005-descubrimiento-y-solicitud-de-citas]]; depende de [[HU-016-solicitar-cita-especializada]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** transición autorizada, liberación y auditoría consistente.
## Tareas de desarrollo
- [x] **T-01 — Implementar transición explícita.** Dificultad: Alto. Validar origen y actor.
- [x] **T-02 — Liberar al rechazo.** Dificultad: Alto. Operación atómica y pruebas.
- [x] **T-03 — Integrar decisión ADMIN.** Dificultad: Medio. Motivo obligatorio en rechazo.
## Criterios de aceptación
### CA-01 — Aprobación
**Dado** una solicitud `REQUESTED` **Cuando** ADMIN aprueba **Entonces** pasa a `APPROVED` y conserva sus slots.
### CA-02 — Rechazo motivado
**Dado** una solicitud `REQUESTED` **Cuando** ADMIN rechaza sin motivo **Entonces** se rechaza la decisión; con motivo pasa a `REJECTED` y libera slots.
### CA-03 — Transición auditable
**Dado** una decisión válida **Cuando** se consulta el historial **Entonces** se registra estado, actor, fuente, fecha/hora y motivo aplicable.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Autorización, transición/liberación atómica, auditoría, pruebas y contrato verificadas.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumplido | [S3 Red→Green](../../../evidence/S3-booking-red-green.md), `BookingIntegrationTest.adminApprovesOrRejectsRequestedAppointmentsAndKeepsAudit` | ADMIN aprueba y la solicitud conserva sus slots. |
| CA-02 | Cumplido | [S3 Red→Green](../../../evidence/S3-booking-red-green.md), `BookingIntegrationTest.adminApprovesOrRejectsRequestedAppointmentsAndKeepsAudit` | Rechazo sin motivo devuelve `400`; con motivo libera slots y pasa a `REJECTED`. |
| CA-03 | Cumplido | [Contrato de reservas](../../../contracts/appointments.md), `BookingIntegrationTest.adminApprovesOrRejectsRequestedAppointmentsAndKeepsAudit` | Historial conserva estado, actor, fuente, fecha y motivo aplicable. |
| DoD | Cumplido | [Contrato](../../../contracts/appointments.md), [evidencia S3](../../../evidence/S3-booking-red-green.md) | Autorización y transición administrativa revalidadas con pruebas. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-29 — Aprobada y cerrada con CA/DoD validados contra la implementación S3 y su evidencia.
## Notas y decisiones
- Las transiciones permitidas (`REQUESTED → APPROVED|REJECTED`) están documentadas en el contrato S3.
