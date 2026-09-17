---
id: HU-020
tipo: historia-de-usuario
titulo: "Solicitar reprogramación"
estado: Pendiente de aprobación
epica: "[[EP-006-gestion-de-citas-del-usuario]]"
esfuerzo: Muy alto
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-018-consultar-mis-citas]]", "[[HU-014-consultar-disponibilidad-para-reserva]]"]
relacionadas: ["[[HU-021-decidir-reprogramacion]]"]
---
# HU-020 — Solicitar reprogramación
## Historia de usuario
**COMO** USER **QUIERO** proponer una nueva fecha/hora disponible para una cita aprobada futura **PARA** cambiarla sin perder mi cita original mientras ADMIN decide.
## Alcance
- Solicitud `PENDING`, conserva profesional/especialidad, retiene nueva franja y conserva original.
## Fuera de alcance
- Cambiar profesional; se trata como nueva cita.
## Reglas de negocio
- RN-01, RN-05, RN-10; solo aprobada/futura; nueva franja completa y original intacta.
## Dependencias y relaciones
- Épica: [[EP-006-gestion-de-citas-del-usuario]]; depende de [[HU-018-consultar-mis-citas]], [[HU-014-consultar-disponibilidad-para-reserva]].
## Esfuerzo
**Nivel:** Muy alto. **Justificación:** mantiene dos franjas, estado, concurrencia y auditoría; debe revisarse para dividir diseño/modelo y flujo si crece.
## Tareas de desarrollo
- [ ] **T-01 — Modelar solicitud y retención provisional.** Dificultad: Alto. Migración 3FN, relación a cita original/nueva franja.
- [ ] **T-02 — Crear solicitud atómica.** Dificultad: Alto. Validar elegibilidad, profesional/especialidad y slots.
- [ ] **T-03 — Integrar flujo de selección.** Dificultad: Alto. UX aprobada con estados y conflicto.
## Criterios de aceptación
### CA-01 — Solicitud elegible
**Dado** una cita propia `APPROVED` y futura **Cuando** USER selecciona nueva franja válida con mismo profesional/especialidad **Entonces** se crea reprogramación `PENDING` y se retiene la nueva franja.
### CA-02 — Original conservada
**Dado** una reprogramación pendiente **Cuando** se consulta la cita original **Entonces** conserva su franja hasta una aprobación ADMIN.
### CA-03 — Restricciones
**Dado** una cita no elegible, cambio de profesional o franja incompleta/conflictiva **Cuando** se solicita reprogramación **Entonces** no se crea solicitud ni retención parcial.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración, modelo 3FN, concurrencia, ownership, pruebas integración y contrato verificadas.
- [ ] La división de esta HU se reconsideró si el diseño aprobado mantiene complejidad muy alta.
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
- Candidata a subdivisión tras acordar modelo/retención; no se implementa hasta resolverlo.
