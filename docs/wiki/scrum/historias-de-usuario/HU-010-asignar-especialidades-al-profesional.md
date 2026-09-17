---
id: HU-010
tipo: historia-de-usuario
titulo: "Asignar especialidades al profesional"
estado: Pendiente de aprobación
epica: "[[EP-003-administracion-de-profesionales]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 2"
dependencias: ["[[HU-008-gestionar-especialidades]]", "[[HU-009-crear-y-administrar-profesional]]"]
relacionadas: ["[[HU-014-consultar-disponibilidad-para-reserva]]"]
---
# HU-010 — Asignar especialidades al profesional
## Historia de usuario
**COMO** ADMIN **QUIERO** asignar una o varias especialidades y marcar una primaria **PARA** definir qué citas puede ofrecer cada profesional.
## Alcance
- Relación N:M, alta/retiro y especialidad primaria.
## Fuera de alcance
- Cambiar duración definida por especialidad.
## Reglas de negocio
- Solo especialidades activas; la reserva exige asociación; una primaria coherente por profesional.
## Dependencias y relaciones
- Épica: [[EP-003-administracion-de-profesionales]]; depende de [[HU-008-gestionar-especialidades]], [[HU-009-crear-y-administrar-profesional]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** N:M y regla de primaria con impacto en disponibilidad.
## Tareas de desarrollo
- [ ] **T-01 — Modelar asociación y primaria.** Dificultad: Medio. Migración, FKs y restricciones.
- [ ] **T-02 — Administrar asignaciones.** Dificultad: Medio. Autorizar y validar catálogo/profesional activo.
- [ ] **T-03 — Validar consumidores.** Dificultad: Medio. Pruebas para oferta y reserva.
## Criterios de aceptación
### CA-01 — Múltiples especialidades
**Dado** un profesional y especialidades activas **Cuando** ADMIN las asigna **Entonces** quedan asociadas sin listas en columnas y puede marcar una primaria.
### CA-02 — Oferta válida
**Dado** una especialidad no asociada o inactiva **Cuando** se busca/reserva con ese profesional **Entonces** no se ofrece como opción reservable.
### CA-03 — Integridad de retiro
**Dado** una asociación requerida por datos comprometidos **Cuando** se intenta retirar **Entonces** se protege la integridad según la decisión/modelo aprobado.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración N:M 3FN, reglas de primaria, autorización y pruebas aplicables verificadas.
- [ ] Contrato y trazabilidad Scrum actualizados.
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
- Regla de retiro con citas existentes queda abierta.
