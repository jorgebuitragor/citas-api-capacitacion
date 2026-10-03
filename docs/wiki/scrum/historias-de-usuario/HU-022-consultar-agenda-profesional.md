---
id: HU-022
tipo: historia-de-usuario
titulo: "Consultar agenda profesional"
estado: Completada
epica: "[[EP-007-operacion-clinica-del-profesional]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-015-reservar-cita-general]]", "[[HU-017-decidir-cita-especializada]]"]
relacionadas: ["[[HU-023-cerrar-atencion]]"]
---
# HU-022 — Consultar agenda profesional
## Historia de usuario
**COMO** PROFESSIONAL **QUIERO** ver mis citas `APPROVED` por día/semana y sede **PARA** preparar mi atención sin consultar datos ajenos.
## Alcance
- Agenda propia filtrable por periodo/sede con citas aprobadas.
## Fuera de alcance
- Citas ajenas e historia clínica.
## Reglas de negocio
- Ownership; solo `APPROVED`; mínima exposición de datos necesaria para propia cita.
## Dependencias y relaciones
- Épica: [[EP-007-operacion-clinica-del-profesional]]; depende de [[HU-015-reservar-cita-general]], [[HU-017-decidir-cita-especializada]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** filtros, ownership y proyección segura.
## Tareas de desarrollo
- [x] **T-01 — Definir consulta propia.** Dificultad: Medio. Filtros y contrato.
- [x] **T-02 — Proteger datos/ownership.** Dificultad: Medio. Pruebas negativas.
- [x] **T-03 — Integrar calendario agenda.** Dificultad: Medio. Estados y accesibilidad.
## Criterios de aceptación
### CA-01 — Agenda filtrada
**Dado** un PROFESSIONAL con citas aprobadas **Cuando** selecciona día/semana y sede **Entonces** ve solo sus citas `APPROVED` en ese criterio.
### CA-02 — Sin datos ajenos
**Dado** otro profesional/usuario **Cuando** intento consultar su agenda **Entonces** el acceso es denegado.
## Definition of Done
- [x] CA-01 y CA-02 validados con evidencia.
- [x] Ownership, proyección de datos, contrato, pruebas y UI aplicables verificadas.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `BookingIntegrationTest.professionalSeesOnlyOwnApprovedAgendaFilteredByDayWeekAndLocation` | PROFESSIONAL ve únicamente sus citas `APPROVED`, filtrables por día/semana y sede opcional; ownership resuelto desde el JWT. |
| CA-02 | Cumple | Mismo test (agenda de otro profesional vacía); `BookingIntegrationTest.agendaAndClosureRequireProfessionalRole` (rol) | Agenda ajena queda vacía/denegada; rol no PROFESSIONAL recibe `403`. |
| DoD | Cumple | `docs/contracts/appointments.md`; backend 25/25 (en la sesión original); `docs/evidence/goals-loops/S4/HU-022-HU-023-implementation.md` | Verificación manual end-to-end en navegador real contra backend y MySQL reales documentada en esa evidencia. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-29 — DEC-006: el usuario aprueba el inicio de implementación en el chat del goal S4.
- 2026-09-29 — Implementada y verificada (backend, frontend y manual end-to-end); evidencia en `docs/evidence/goals-loops/S4/HU-022-HU-023-implementation.md`.
- 2026-10-02 — El usuario instruye marcar formalmente `Completada`; se actualiza este campo y la tabla de evidencia, que habían quedado pendientes de una ejecución de `scrum-spec-orchestrator`.
## Notas y decisiones
- Campos exactos expuestos se pactan en contrato con minimización de datos.
