---
id: HU-018
tipo: historia-de-usuario
titulo: "Consultar mis citas"
estado: Completada
epica: "[[EP-006-gestion-de-citas-del-usuario]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-015-reservar-cita-general]]", "[[HU-016-solicitar-cita-especializada]]"]
relacionadas: ["[[HU-019-cancelar-cita]]", "[[HU-020-solicitar-reprogramacion]]"]
---
# HU-018 — Consultar mis citas
## Historia de usuario
**COMO** USER **QUIERO** listar y filtrar mis citas por estado/fecha y ver su detalle **PARA** gestionar mis atenciones.
## Alcance
- Mostrar sede, profesional, especialidad, fecha/hora, duración, estado y motivo de rechazo cuando exista.
## Fuera de alcance
- Consultar citas de otros usuarios.
## Reglas de negocio
- Ownership; no repetir datos de catálogo en la cita si existe FK; se presenta el dato relacionado.
## Dependencias y relaciones
- Épica: [[EP-006-gestion-de-citas-del-usuario]]; depende de [[HU-015-reservar-cita-general]], [[HU-016-solicitar-cita-especializada]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** proyección filtrada, ownership y estados de UI.
## Tareas de desarrollo
- [x] **T-01 — Definir consulta propia.** Dificultad: Medio. Filtros, orden y contrato.
- [x] **T-02 — Aplicar ownership.** Dificultad: Medio. Pruebas de acceso indebido.
- [x] **T-03 — Implementar listado/detalle.** Dificultad: Medio. Loading, empty, error y accesibilidad.
## Criterios de aceptación
### CA-01 — Información mínima
**Dado** citas propias **Cuando** USER las consulta **Entonces** ve todos los campos mínimos y el motivo de rechazo cuando corresponde.
### CA-02 — Filtros
**Dado** citas en distintas fechas/estados **Cuando** filtra por estado o fecha **Entonces** el resultado respeta los filtros.
### CA-03 — Ownership
**Dado** una cita de otro USER **Cuando** intento consultarla **Entonces** no se expone.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Contrato, ownership, pruebas y estados de interfaz aplicables verificados.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `BookingIntegrationTest.userListsOnlyOwnAppointmentsAndCanFilterByStatusAndDate`; `src/components/MyAppointments.tsx` | Respuesta y tarjeta muestran sede, profesional, especialidad, fecha/hora, duración, estado y motivo solo para REJECTED. |
| CA-02 | Cumple | `BookingIntegrationTest.userListsOnlyOwnAppointmentsAndCanFilterByStatusAndDate`; `src/api/booking.ts` | El test valida filtros status/date; el cliente serializa ambos filtros opcionales. |
| CA-03 | Cumple | `BookingIntegrationTest.userListsOnlyOwnAppointmentsAndCanFilterByStatusAndDate` | La respuesta del USER no incluye la cita creada por otro usuario; consulta siempre filtra con `principal.getName()`. |
| DoD | Cumple | `docs/contracts/appointments.md`; backend 15/15; frontend typecheck, Vitest 8/8 y build; `src/components/MyAppointments.test.tsx` | Estados loading/empty/error/success implementados; contrato productor-consumidor verificado por pruebas y build. Sin cambio de esquema. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-29 — Aprobada explícitamente por instrucción del usuario; criterios y DoD se conservan sin cambios.
- 2026-09-29 — Validada y completada con evidencia de implementación y verificaciones backend/frontend registradas en S4 checkpoint.
## Notas y decisiones
- Paginar/ordenar requiere contrato, no está exigido por PRD.

