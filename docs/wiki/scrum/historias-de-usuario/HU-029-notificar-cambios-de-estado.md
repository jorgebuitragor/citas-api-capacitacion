---
id: HU-029
tipo: historia-de-usuario
titulo: "Notificar cambios de estado"
estado: Pendiente de aprobación
epica: "[[EP-010-automatizaciones-posteriores]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 6"
dependencias: ["[[HU-025-consultar-auditoria-de-estados]]"]
relacionadas: ["[[HU-017-decidir-cita-especializada]]", "[[HU-021-decidir-reprogramacion]]"]
---
# HU-029 — Notificar cambios de estado
## Historia de usuario
**COMO** USER afectado **QUIERO** recibir notificación cuando cambia el estado de mi cita **PARA** conocer la decisión u operación relevante.
## Alcance
- Webhook + Gmail mediante n8n para cambio de estado.
## Fuera de alcance
- Modificar transiciones del núcleo o exponer tokens/credenciales.
## Reglas de negocio
- La fuente se deriva de eventos auditables; JSON n8n versionado sin secretos.
## Dependencias y relaciones
- Épica: [[EP-010-automatizaciones-posteriores]]; depende de [[HU-025-consultar-auditoria-de-estados]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** evento/webhook, filtrado de datos y entorno externo.
## Tareas de desarrollo
- [ ] **T-01 — Definir evento/payload mínimo.** Dificultad: Medio. No exponer datos innecesarios.
- [ ] **T-02 — Configurar/exportar workflow.** Dificultad: Medio. Webhook/Gmail según entorno.
- [ ] **T-03 — Verificar transición sintética.** Dificultad: Medio. Evidencia y manejo de fallos.
## Criterios de aceptación
### CA-01 — Cambio notificado
**Dado** un cambio de estado elegible **Cuando** se emite el evento/webhook configurado **Entonces** el workflow genera la notificación correspondiente.
### CA-02 — Datos y secretos protegidos
**Dado** el payload/workflow **Cuando** se revisa **Entonces** solo contiene información necesaria/sintética y no credenciales versionadas.
## Definition of Done
- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] JSON n8n, evento, prueba sintética y tratamiento de fallo aplicable documentados.
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
- Estados notificables y formato del mensaje requieren aprobación.
