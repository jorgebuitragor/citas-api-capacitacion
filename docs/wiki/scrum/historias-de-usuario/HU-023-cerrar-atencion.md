---
id: HU-023
tipo: historia-de-usuario
titulo: "Cerrar atención"
estado: Pendiente de aprobación
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
- [ ] **T-01 — Definir elegibilidad/transición.** Dificultad: Medio. Acordar “aplicable”.
- [ ] **T-02 — Registrar resultado/auditoría.** Dificultad: Medio. Prohibir edición CRUD.
- [ ] **T-03 — Integrar acción de agenda.** Dificultad: Bajo. Estados y confirmación.
## Criterios de aceptación
### CA-01 — Cierre propio válido
**Dado** una cita propia aprobada pasada/aplicable **Cuando** PROFESSIONAL la marca **Entonces** queda `COMPLETED` o `NO_SHOW` y se audita.
### CA-02 — Cierre inválido
**Dado** una cita futura, ajena o no elegible **Cuando** intento cerrarla **Entonces** no cambia.
## Definition of Done
- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] Ownership, transición/auditoría, pruebas y contrato aplicables verificados.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
## Notas y decisiones
- “Pasada/aplicable” es una pregunta abierta explícita.
