---
id: HU-015
tipo: historia-de-usuario
titulo: "Reservar cita general"
estado: Aprobada
epica: "[[EP-005-descubrimiento-y-solicitud-de-citas]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 3"
dependencias: ["[[HU-014-consultar-disponibilidad-para-reserva]]"]
relacionadas: ["[[HU-018-consultar-mis-citas]]", "[[HU-025-consultar-auditoria-de-estados]]"]
---
# HU-015 — Reservar cita general
## Historia de usuario
**COMO** USER **QUIERO** confirmar una franja disponible de Medicina General con un profesional general **PARA** obtener una cita aprobada de inmediato.
## Alcance
- Selección y confirmación de cita general disponible; estado `APPROVED` automático.
## Fuera de alcance
- Aprobación ADMIN o reserva especializada.
## Reglas de negocio
- RN-01, RN-02, RN-06, RN-08; no doble reserva y transición auditada.
## Dependencias y relaciones
- Épica: [[EP-005-descubrimiento-y-solicitud-de-citas]]; depende de [[HU-014-consultar-disponibilidad-para-reserva]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** operación transaccional/concurrente, estado y auditoría.
## Tareas de desarrollo
- [x] **T-01 — Modelar cita/ocupación/auditoría.** Dificultad: Alto. Migración 3FN e índices.
- [x] **T-02 — Ejecutar reserva atómica.** Dificultad: Alto. Comprobar disponibilidad al confirmar y prevenir carreras.
- [x] **T-03 — Integrar confirmación.** Dificultad: Medio. Evitar doble envío y comunicar conflicto.
## Criterios de aceptación
### CA-01 — Aprobación automática
**Dado** una franja general aún disponible **Cuando** USER confirma **Entonces** se crea una cita `APPROVED` con profesional general y slots ocupados.
### CA-02 — Conflicto al confirmar
**Dado** que otro proceso ocupó la franja antes de confirmar **Cuando** USER intenta reservar **Entonces** no se crea cita duplicada y recibe respuesta contractual de conflicto.
### CA-03 — Auditoría
**Dado** una reserva exitosa **Cuando** se revisa su historial **Entonces** existe evento de estado con fuente/fecha/hora y actor aplicable.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Migración, atomicidad/concurrencia aprobada, pruebas integración y contrato consumido verificadas.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumplido | [S3 Red→Green](../../../evidence/S3-booking-red-green.md), `BookingIntegrationTest.rejectsSecondReservationWithoutCreatingAnotherAppointment` | Reserva general `APPROVED`, ocupa un slot y queda auditada. |
| CA-02 | Cumplido | [S3 Red→Green](../../../evidence/S3-booking-red-green.md), `BookingIntegrationTest.rejectsSecondReservationWithoutCreatingAnotherAppointment` | Segunda reserva responde `409` y no crea una cita adicional. |
| CA-03 | Cumplido | [Contrato de reservas](../../../contracts/appointments.md), `BookingIntegrationTest.rejectsSecondReservationWithoutCreatingAnotherAppointment` | Historial `APPROVED` con fuente `SYSTEM`, fecha y actor nulo. |
| DoD | Cumplido | [Contrato](../../../contracts/appointments.md), [evidencia S3](../../../evidence/S3-booking-red-green.md) | Pruebas API/frontend, migraciones y hooks revalidados. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-29 — Aprobada y cerrada con CA/DoD validados contra la implementación S3 y su evidencia.
## Notas y decisiones
- Estrategia aprobada en DEC-003: bloqueo pesimista y asignación atómica de slots.
