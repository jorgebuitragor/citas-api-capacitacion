---
id: HU-012
tipo: historia-de-usuario
titulo: "Gestionar bloques de disponibilidad"
estado: Pendiente de aprobación
epica: "[[EP-004-disponibilidad-del-profesional]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 3"
dependencias: ["[[HU-009-crear-y-administrar-profesional]]", "[[HU-011-asignar-sedes-al-profesional]]"]
relacionadas: ["[[HU-014-consultar-disponibilidad-para-reserva]]"]
---
# HU-012 — Gestionar bloques de disponibilidad
## Historia de usuario
**COMO** PROFESSIONAL **QUIERO** crear, editar o eliminar mis bloques futuros por sede **PARA** publicar una agenda reservable.
## Alcance
- Múltiples bloques por día, sede, edición/eliminación futura sin citas comprometidas, discretización a 30 min.
## Fuera de alcance
- Publicar bloques en pasado, solapados o en sede no asignada.
## Reglas de negocio
- No pasado, no solapes del mismo profesional, sede habilitada; bloque comprometido no se modifica/elimina.
## Dependencias y relaciones
- Épica: [[EP-004-disponibilidad-del-profesional]]; depende de [[HU-009-crear-y-administrar-profesional]], [[HU-011-asignar-sedes-al-profesional]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** validación temporal, solapes, ownership y reserva posterior.
## Tareas de desarrollo
- [ ] **T-01 — Modelar bloques/slots.** Dificultad: Alto. Definir 3FN e índices de agenda.
- [ ] **T-02 — Implementar reglas de agenda.** Dificultad: Alto. Casos de uso y pruebas temporales/solape.
- [ ] **T-03 — Integrar calendario aprobado.** Dificultad: Medio. Formularios y estados visuales.
## Criterios de aceptación
### CA-01 — Bloque futuro válido
**Dado** un profesional activo con sede asignada **Cuando** registra un bloque futuro no solapado **Entonces** queda disponible en incrementos de 30 minutos.
### CA-02 — Bloque inválido
**Dado** una fecha pasada, sede no asignada o solape propio **Cuando** intento crear/editar **Entonces** se rechaza sin alterar agenda válida.
### CA-03 — Protección de compromiso
**Dado** un bloque futuro con cita comprometida **Cuando** intento editarlo/eliminarlo **Entonces** el sistema impide la operación.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración/índices, ownership, pruebas de pasado-solape-compromiso y contrato verificados.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| CA-03 | Pendiente | — | — |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
## Notas y decisiones
- “Cita comprometida” debe incluir estados/retenciones aprobados en el contrato.
