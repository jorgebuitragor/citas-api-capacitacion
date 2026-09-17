---
id: HU-011
tipo: historia-de-usuario
titulo: "Asignar sedes al profesional"
estado: Pendiente de aprobación
epica: "[[EP-003-administracion-de-profesionales]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 2"
dependencias: ["[[HU-001-consultar-catalogos-fijos]]", "[[HU-009-crear-y-administrar-profesional]]"]
relacionadas: ["[[HU-012-gestionar-bloques-de-disponibilidad]]"]
---
# HU-011 — Asignar sedes al profesional
## Historia de usuario
**COMO** ADMIN **QUIERO** asignar una o ambas sedes fijas a un profesional **PARA** limitar dónde puede publicar y atender.
## Alcance
- Relación N:M profesional-sede y mantenimiento autorizado.
## Fuera de alcance
- CRUD de sedes fijas.
## Reglas de negocio
- Un bloque solo usa sede asignada; las sedes son catálogo fijo.
## Dependencias y relaciones
- Épica: [[EP-003-administracion-de-profesionales]]; depende de [[HU-001-consultar-catalogos-fijos]], [[HU-009-crear-y-administrar-profesional]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** N:M y regla que protege agenda/reserva.
## Tareas de desarrollo
- [ ] **T-01 — Modelar asociación de sedes.** Dificultad: Medio. Migración/FKs 3FN.
- [ ] **T-02 — Gestionar asignación por ADMIN.** Dificultad: Medio. Validación y autorización.
- [ ] **T-03 — Aplicar restricción en agenda.** Dificultad: Medio. Pruebas de borde.
## Criterios de aceptación
### CA-01 — Una o ambas sedes
**Dado** un profesional válido **Cuando** ADMIN asigna HIC, ICV o ambas **Entonces** las asignaciones quedan persistidas y consultables.
### CA-02 — Sede no habilitada
**Dado** una sede no asignada **Cuando** el profesional intenta publicar un bloque allí **Entonces** el sistema lo rechaza.
### CA-03 — Solo ADMIN
**Dado** un rol distinto de ADMIN **Cuando** intenta modificar asignaciones **Entonces** recibe denegación autorizada.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración N:M, autorización, regla de agenda y pruebas aplicables verificadas.
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
- Retiro de sede con citas futuras requiere decisión.
