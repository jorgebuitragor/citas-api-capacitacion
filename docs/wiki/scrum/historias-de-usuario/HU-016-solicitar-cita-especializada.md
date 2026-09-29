---
id: HU-016
tipo: historia-de-usuario
titulo: "Solicitar cita especializada"
estado: Aprobada
epica: "[[EP-005-descubrimiento-y-solicitud-de-citas]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 3"
dependencias: ["[[HU-014-consultar-disponibilidad-para-reserva]]"]
relacionadas: ["[[HU-017-decidir-cita-especializada]]"]
---
# HU-016 — Solicitar cita especializada
## Historia de usuario
**COMO** USER **QUIERO** solicitar una franja de especialidad, sede y profesional **PARA** que ADMIN decida una cita especializada sin que otra persona tome el horario.
## Alcance
- Solicitud especializada `REQUESTED` y retención de toda franja requerida.
## Fuera de alcance
- Aprobación/rechazo por ADMIN.
## Reglas de negocio
- RN-01, RN-03, RN-05, RN-08; 60 minutos usa dos slots consecutivos; retención evita doble reserva.
## Dependencias y relaciones
- Épica: [[EP-005-descubrimiento-y-solicitud-de-citas]]; depende de [[HU-014-consultar-disponibilidad-para-reserva]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** retención concurrente y estado pendiente.
## Tareas de desarrollo
- [x] **T-01 — Extender modelo de solicitud/reserva.** Dificultad: Alto. Migración y relación de slots.
- [x] **T-02 — Crear solicitud atómica.** Dificultad: Alto. Verificar toda la duración y retenerla.
- [x] **T-03 — Integrar solicitud UX.** Dificultad: Medio. Confirmación y error de conflicto.
## Criterios de aceptación
### CA-01 — Solicitud retenida
**Dado** una franja especializada disponible **Cuando** USER la solicita **Entonces** nace `REQUESTED` y sus slots quedan retenidos.
### CA-02 — Duración íntegra
**Dado** especialidad de 60 minutos **Cuando** no hay dos slots consecutivos libres **Entonces** no se crea la solicitud ni una retención parcial.
### CA-03 — No doble reserva
**Dado** una franja retenida **Cuando** otro USER intenta tomarla **Entonces** no se ofrece o confirma para la segunda solicitud.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Migración, concurrencia/retención aprobada, pruebas integración y contrato verificadas.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumplido | [S3 Red→Green](../../../evidence/S3-booking-red-green.md), `BookingIntegrationTest.specializedRequestReservesTwoConsecutiveSlotsAndRejectsPartialDuration` | Solicitud `REQUESTED` y slots de la franja retenidos. |
| CA-02 | Cumplido | [S3 Red→Green](../../../evidence/S3-booking-red-green.md), `BookingIntegrationTest.specializedRequestReservesTwoConsecutiveSlotsAndRejectsPartialDuration` | Especialidad de 60 minutos exige dos slots y no deja retención parcial. |
| CA-03 | Cumplido | [S3 Red→Green](../../../evidence/S3-booking-red-green.md), `BookingIntegrationTest.secondSpecializedReservationCannotUseHeldSlots` | Segunda solicitud especializada responde `409`; no se crea cita adicional. |
| DoD | Cumplido | [Contrato](../../../contracts/appointments.md), [evidencia S3](../../../evidence/S3-booking-red-green.md) | Retención, concurrencia, pruebas y consumidor frontend verificados. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-29 — Aprobada y cerrada con CA/DoD validados contra la implementación S3 y su evidencia.
## Notas y decisiones
- La retención sin expiración durante S3 está aprobada en DEC-003; cualquier política posterior queda fuera de esta HU.
