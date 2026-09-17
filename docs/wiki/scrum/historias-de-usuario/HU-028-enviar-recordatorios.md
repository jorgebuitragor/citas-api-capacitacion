---
id: HU-028
tipo: historia-de-usuario
titulo: "Enviar recordatorios de citas"
estado: Pendiente de aprobación
epica: "[[EP-010-automatizaciones-posteriores]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 6"
dependencias: ["[[HU-015-reservar-cita-general]]", "[[HU-017-decidir-cita-especializada]]"]
relacionadas: []
---
# HU-028 — Enviar recordatorios de citas
## Historia de usuario
**COMO** USER con cita próxima **QUIERO** recibir un recordatorio automatizado **PARA** recordar mi atención ficticia programada.
## Alcance
- Workflow n8n/Gmail posterior para citas próximas, con datos sintéticos.
## Fuera de alcance
- Cambiar el núcleo de citas, SMS/WhatsApp y versionar credenciales.
## Reglas de negocio
- JSON exportado en `citas-api/automations/n8n/`; credenciales solo en entorno; no datos reales.
## Dependencias y relaciones
- Épica: [[EP-010-automatizaciones-posteriores]]; depende de [[HU-015-reservar-cita-general]], [[HU-017-decidir-cita-especializada]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** selección operativa y configuración externa de n8n/Gmail.
## Tareas de desarrollo
- [ ] **T-01 — Especificar disparador/selección.** Dificultad: Medio. Acordar “próxima” y fuente de datos.
- [ ] **T-02 — Configurar/exportar workflow.** Dificultad: Medio. Sin secretos en JSON.
- [ ] **T-03 — Verificar con datos sintéticos.** Dificultad: Medio. Evidencia de ejecución del entorno autorizado.
## Criterios de aceptación
### CA-01 — Selección de cita próxima
**Dado** una cita elegible próxima **Cuando** corre el workflow configurado **Entonces** prepara/envía el recordatorio según configuración autorizada.
### CA-02 — Versionado seguro
**Dado** el workflow final **Cuando** se versiona **Entonces** su JSON está en la ubicación requerida y no contiene credenciales/datos reales.
## Definition of Done
- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] JSON n8n exportado, configuración externa y prueba sintética documentadas.
- [ ] No se cambió el núcleo funcional; trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
## Notas y decisiones
- Momento/destinatario del recordatorio no está definido por PRD.
