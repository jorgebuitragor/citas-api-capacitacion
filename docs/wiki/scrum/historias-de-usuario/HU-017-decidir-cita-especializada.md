---
id: HU-017
tipo: historia-de-usuario
titulo: "Decidir cita especializada"
estado: Pendiente de aprobación
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
- [ ] **T-01 — Implementar transición explícita.** Dificultad: Alto. Validar origen y actor.
- [ ] **T-02 — Liberar al rechazo.** Dificultad: Alto. Operación atómica y pruebas.
- [ ] **T-03 — Integrar decisión ADMIN.** Dificultad: Medio. Motivo obligatorio en rechazo.
## Criterios de aceptación
### CA-01 — Aprobación
**Dado** una solicitud `REQUESTED` **Cuando** ADMIN aprueba **Entonces** pasa a `APPROVED` y conserva sus slots.
### CA-02 — Rechazo motivado
**Dado** una solicitud `REQUESTED` **Cuando** ADMIN rechaza sin motivo **Entonces** se rechaza la decisión; con motivo pasa a `REJECTED` y libera slots.
### CA-03 — Transición auditable
**Dado** una decisión válida **Cuando** se consulta el historial **Entonces** se registra estado, actor, fuente, fecha/hora y motivo aplicable.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Autorización, transición/libración atómica, auditoría, pruebas y contrato verificadas.
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
- El conjunto exacto de transiciones permitidas debe documentarse en contrato.
