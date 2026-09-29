---
id: HU-014
tipo: historia-de-usuario
titulo: "Consultar disponibilidad para reserva"
estado: Completada
epica: "[[EP-005-descubrimiento-y-solicitud-de-citas]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 3"
dependencias: ["[[HU-008-gestionar-especialidades]]", "[[HU-010-asignar-especialidades-al-profesional]]", "[[HU-012-gestionar-bloques-de-disponibilidad]]"]
relacionadas: ["[[HU-015-reservar-cita-general]]", "[[HU-016-solicitar-cita-especializada]]"]
---
# HU-014 — Consultar disponibilidad para reserva
## Historia de usuario
**COMO** USER **QUIERO** filtrar horarios por sede, tipo de cita, especialidad, profesional y fecha **PARA** elegir una franja que complete la atención requerida.
## Alcance
- Filtros solicitados y resultado de slots reservables de 30/60 min.
## Fuera de alcance
- Crear la reserva o mostrar slots no completables.
## Reglas de negocio
- Especialidad activa/asociada; 60 min requiere dos slots consecutivos; no se muestran retenidos/reservados.
## Dependencias y relaciones
- Épica: [[EP-005-descubrimiento-y-solicitud-de-citas]]; depende de [[HU-008-gestionar-especialidades]], [[HU-010-asignar-especialidades-al-profesional]], [[HU-012-gestionar-bloques-de-disponibilidad]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** filtros y cálculo de slots con consistencia frente a reservas.
## Tareas de desarrollo
- [ ] **T-01 — Diseñar consulta de disponibilidad.** Dificultad: Alto. Índices/puertos, filtros y duración.
- [ ] **T-02 — Excluir conflictos.** Dificultad: Alto. Aplicar reservas y retenciones aprobadas.
- [ ] **T-03 — Integrar búsqueda aprobada.** Dificultad: Medio. Estados loading/empty/error.
## Criterios de aceptación
### CA-01 — Filtros
**Dado** disponibilidad publicada **Cuando** USER aplica los filtros definidos **Entonces** recibe resultados coherentes con ellos.
### CA-02 — Duración completa
**Dado** una especialidad de 30 o 60 minutos **Cuando** se muestran horarios **Entonces** solo aparecen franjas de uno o dos slots consecutivos libres, respectivamente.
### CA-03 — Exclusión de conflicto
**Dado** slots reservados o retenidos **Cuando** se consulta disponibilidad **Entonces** no se ofrecen para una nueva reserva.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Pruebas de filtros, duración, conflictos y contrato frontend-backend aplicables verificadas.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `BookingIntegrationTest.userCanSearchOnlyCompleteAvailabilityUsingAllFilters`; [contrato](../../../contracts/appointments.md) | `GET /api/v1/availability` exige sede, especialidad, profesional y fecha; el consumidor web envía los cuatro filtros. |
| CA-02 | Cumple | `BookingService.contiguousAvailable`; `BookingIntegrationTest.specializedRequestReservesTwoConsecutiveSlotsAndRejectsPartialDuration` | Solo se ofrecen franjas de uno o dos slots consecutivos según la duración de la especialidad. |
| CA-03 | Cumple | `BookingIntegrationTest.heldRescheduleSlotIsNotOfferedNorReservable` | La disponibilidad excluye tanto el slot reservado por una cita como el retenido por una reprogramación `PENDING`, y una reserva ajena sobre él responde `409`. |
| DoD | Cumple | [Contrato](../../../contracts/appointments.md), [evidencia S4](../../../evidence/goals-loops/S4/HU-020-HU-021-preflight-checkpoint.md) | API 25/25; frontend typecheck, 23 pruebas Vitest y build correctos. Sin cambio de esquema propio de esta HU. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-29 — Aprobada explícitamente por instrucción del usuario junto con HU-020 y HU-021 (DEC-005). Criterios y DoD se conservan sin cambios.
- 2026-09-29 — Completada. CA-03 quedó cubierto con una aserción explícita de exclusión de slots reservados y retenidos añadida durante la entrega de HU-020/HU-021.
## Notas y decisiones
- La concurrencia y la retención sin expiración quedaron aprobadas en DEC-003; la nota original que pedía definirlas antes de implementar ya no bloquea.
- CA-01 y CA-02 provienen de la implementación S3 (`GET /api/v1/availability`); CA-03 se cerró al introducir la retención de reprogramación, que hace verificable la exclusión de franjas retenidas.
