---
id: HU-007
tipo: historia-de-usuario
titulo: "Gestionar planes de EPS"
estado: Pendiente de aprobación
epica: "[[EP-002-catalogos-configurables]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 2"
dependencias: ["[[HU-006-gestionar-eps]]"]
relacionadas: ["[[HU-005-gestionar-perfil-y-afiliacion]]"]
---
# HU-007 — Gestionar planes de EPS
## Historia de usuario
**COMO** ADMIN **QUIERO** administrar planes asociados a una EPS **PARA** ofrecer afiliaciones consistentes.
## Alcance
- CRUD autorizado de planes vinculados a EPS y activación/desactivación.
## Fuera de alcance
- Asociación de plan a una EPS inexistente o eliminación física referenciada.
## Reglas de negocio
- Plan depende de EPS; catálogos referenciados se desactivan.
## Dependencias y relaciones
- Épica: [[EP-002-catalogos-configurables]]; depende de [[HU-006-gestionar-eps]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** relación padre-hijo e integridad para afiliación.
## Tareas de desarrollo
- [ ] **T-01 — Modelar relación EPS-plan.** Dificultad: Medio. Migración y FK normalizada.
- [ ] **T-02 — Implementar gestión y validación.** Dificultad: Medio. Garantizar pertenencia plan/EPS.
- [ ] **T-03 — Integrar selección dependiente.** Dificultad: Medio. UX aprobada y estados vacíos/error.
## Criterios de aceptación
### CA-01 — Plan asociado
**Dado** una EPS válida **Cuando** ADMIN crea o modifica un plan válido **Entonces** el plan queda asociado a esa EPS y consultable según contrato.
### CA-02 — Coherencia de afiliación
**Dado** un plan que no pertenece a la EPS seleccionada **Cuando** se intenta usar en una afiliación **Entonces** se rechaza sin guardar una combinación inconsistente.
### CA-03 — Catálogo en uso
**Dado** un plan referenciado **Cuando** ADMIN intenta retirarlo **Entonces** no se borra físicamente y puede desactivarse según la regla aplicable.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] FK, migración, autorización, pruebas de relación y contrato verificados.
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
- La definición de plan activo debe alinearse con la edición de afiliación.
