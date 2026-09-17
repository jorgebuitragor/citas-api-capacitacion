---
id: HU-026
tipo: historia-de-usuario
titulo: "Documentar contrato REST base"
estado: Pendiente de aprobación
epica: "[[EP-009-contrato-rest-y-experiencia-web]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 1"
dependencias: []
relacionadas: ["[[HU-003-iniciar-y-cerrar-sesion]]", "[[HU-027-integrar-pantallas-por-rol]]"]
---
# HU-026 — Documentar contrato REST base
## Historia de usuario
**COMO** equipo de producto **QUIERO** acordar y documentar el contrato REST/JSON de los flujos aprobados **PARA** que frontend y backend evolucionen de forma compatible.
## Alcance
- Recursos, payloads, errores, autorización, ownership, CORS y compatibilidad para capacidades seleccionadas.
## Fuera de alcance
- Implementar endpoints o asumir framework UI.
## Reglas de negocio
- REST directo a `citas-api`, sin Express/BFF; backend es autoridad; URL web configurable por environment.
## Dependencias y relaciones
- Épica: [[EP-009-contrato-rest-y-experiencia-web]]; relacionada: [[HU-003-iniciar-y-cerrar-sesion]], [[HU-027-integrar-pantallas-por-rol]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** frontera cross-repo y decisiones no definidas aún.
## Tareas de desarrollo
- [ ] **T-01 — Especificar recursos y errores.** Dificultad: Alto. Mapear HU, roles y reglas.
- [ ] **T-02 — Acordar seguridad/transición.** Dificultad: Alto. JWT, refresh, CORS, ownership y estados.
- [ ] **T-03 — Validar productor/consumidor.** Dificultad: Alto. Evidencia de contrato clave.
## Criterios de aceptación
### CA-01 — Contrato rastreable
**Dado** una HU aprobada que cruza web/API **Cuando** se prepara su implementación **Entonces** request, response, errores, autorización y consumidor están documentados y enlazados.
### CA-02 — Sin supuestos ocultos
**Dado** decisiones no definidas por fuentes (tokens, errores, retención) **Cuando** afectan un flujo **Entonces** se registran como decisiones/preguntas, no como comportamiento inventado.
## Definition of Done
- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] Documentación enlazada entre Scrum y contrato; validación backend-consumidor aplicable registrada.
- [ ] No se modificó código como parte de esta HU documental.
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
- Esta HU habilita especificación; cada endpoint se aprueba con la HU funcional correspondiente.
