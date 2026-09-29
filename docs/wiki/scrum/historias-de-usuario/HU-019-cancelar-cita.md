---
id: HU-019
tipo: historia-de-usuario
titulo: "Cancelar cita"
estado: Completada
epica: "[[EP-006-gestion-de-citas-del-usuario]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-018-consultar-mis-citas]]"]
relacionadas: ["[[HU-025-consultar-auditoria-de-estados]]"]
---
# HU-019 — Cancelar cita
## Historia de usuario
**COMO** USER **QUIERO** cancelar una cita futura no terminal propia **PARA** liberar el horario que ya no usaré.
## Alcance
- Transición a `CANCELLED`, liberación de slots e historial.
## Fuera de alcance
- Reactivar directamente una cancelada.
## Reglas de negocio
- RN-09 y RN-11; solo futura/no terminal; no reactivación directa.
## Dependencias y relaciones
- Épica: [[EP-006-gestion-de-citas-del-usuario]]; depende de [[HU-018-consultar-mis-citas]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** autorización, estado, slots y auditoría atómicos.
## Tareas de desarrollo
- [x] **T-01 — Definir transición de cancelación.** Dificultad: Alto. Validar ownership/futuro/no terminal.
- [x] **T-02 — Liberar reserva e historial.** Dificultad: Alto. Operación consistente y pruebas.
- [x] **T-03 — Integrar confirmación UX.** Dificultad: Medio. Evitar doble acción.
## Criterios de aceptación
### CA-01 — Cancelación permitida
**Dado** una cita propia futura no terminal **Cuando** USER la cancela **Entonces** queda `CANCELLED`, se liberan slots y se registra historial.
### CA-02 — Cancelación no permitida
**Dado** una cita pasada, terminal o ajena **Cuando** USER intenta cancelarla **Entonces** la operación se rechaza sin cambios.
### CA-03 — Sin reactivación directa
**Dado** una cita `CANCELLED` **Cuando** intento reactivarla **Entonces** el flujo no la restaura directamente.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Transición, liberación, auditoría, ownership y pruebas integración verificadas.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `BookingIntegrationTest.userCancellationReleasesSlotsAddsHistoryAndIsNotRepeatable`; `BookingService.cancel` | Estado CANCELLED, slots liberados y una entrada de historial con actor USER ocurren en la transacción; prueba confirma estado, disponibilidad y actor/evento. |
| CA-02 | Cumple | `BookingIntegrationTest.cannotCancelAnotherUsersOrPastOrRejectedAppointment` | Ownership ajeno responde 404; fecha pasada y estado terminal REJECTED responden 409; estado e historial quedan sin cambios. |
| CA-03 | Cumple | `BookingIntegrationTest.userCancellationReleasesSlotsAddsHistoryAndIsNotRepeatable`; `MyAppointments.tsx` | Segundo intento responde 409; cita cancelada no ofrece acción; el diálogo cliente serializa envíos y evita doble envío. |
| DoD | Cumple | `docs/contracts/appointments.md`; backend 15/15; frontend typecheck, Vitest 8/8 y build; `src/components/MyAppointments.test.tsx` | Transición, ownership, filtros/estados y contrato verificados. No se añadió ventana adicional ni migración. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-29 — Aprobada explícitamente por instrucción del usuario; criterios y DoD se conservan sin cambios.
- 2026-09-29 — Validada y completada con evidencia de implementación y verificaciones backend/frontend registradas en S4 checkpoint.
## Notas y decisiones
- No se definió una ventana adicional de cancelación; no debe inventarse.

