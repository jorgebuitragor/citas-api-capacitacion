---
id: HU-025
tipo: historia-de-usuario
titulo: "Consultar auditoría de estados"
estado: Pendiente de aprobación
epica: "[[EP-008-operacion-administrativa-y-trazabilidad]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 5"
dependencias: ["[[HU-015-reservar-cita-general]]", "[[HU-017-decidir-cita-especializada]]"]
relacionadas: ["[[HU-019-cancelar-cita]]", "[[HU-021-decidir-reprogramacion]]", "[[HU-023-cerrar-atencion]]"]
---
# HU-025 — Consultar auditoría de estados
## Historia de usuario
**COMO** actor autorizado **QUIERO** consultar el historial de estados pertinente de una cita **PARA** verificar cómo cambió y quién/qué lo originó.
## Alcance
- Eventos con cita, estado nuevo, actor cuando existe, fuente SYSTEM/USER/ADMIN, fecha/hora y motivo opcional.
## Fuera de alcance
- CRUD/edición de auditoría y acceso indiscriminado.
## Reglas de negocio
- RN-12: la auditoría no se modifica como CRUD normal; authorization/ownership según rol y contrato.
## Dependencias y relaciones
- Épica: [[EP-008-operacion-administrativa-y-trazabilidad]]; depende de [[HU-015-reservar-cita-general]], [[HU-017-decidir-cita-especializada]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** inmutabilidad, proyección y autorización por actor.
## Tareas de desarrollo
- [ ] **T-01 — Modelar eventos inmutables.** Dificultad: Medio. Migración 3FN y fuente/actor/motivo.
- [ ] **T-02 — Registrar transiciones.** Dificultad: Alto. Integrar casos de uso sin CRUD normal.
- [ ] **T-03 — Publicar consulta autorizada.** Dificultad: Medio. Contrato/pruebas de visibilidad.
## Criterios de aceptación
### CA-01 — Evento completo
**Dado** una transición de cita **Cuando** se consulta su historial autorizado **Entonces** se ve estado nuevo, actor aplicable, fuente, fecha/hora y motivo aplicable.
### CA-02 — Inmutabilidad
**Dado** un evento creado **Cuando** se intenta editar/eliminar como CRUD normal **Entonces** la operación no está disponible/permitida.
### CA-03 — Visibilidad controlada
**Dado** una cita ajena o rol no habilitado **Cuando** se intenta consultar sus eventos **Entonces** no se exponen.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración, inmutabilidad, autorización, pruebas y contrato aplicables verificados.
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
- Matriz exacta de lectura por rol pendiente de contrato.
