---
id: HU-030
tipo: historia-de-usuario
titulo: "Generar resumen operativo"
estado: Pendiente de aprobación
epica: "[[EP-010-automatizaciones-posteriores]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 6"
dependencias: ["[[HU-025-consultar-auditoria-de-estados]]"]
relacionadas: ["[[HU-024-consultar-bandeja-administrativa]]"]
---
# HU-030 — Generar resumen operativo
## Historia de usuario
**COMO** ADMIN/operación autorizada **QUIERO** recibir un resumen diario por sede y estado **PARA** revisar la operación ficticia.
## Alcance
- Caso n8n posterior de resumen por sede/estado usando datos sintéticos.
## Fuera de alcance
- Analítica clínica, datos reales o cambios en las citas.
## Reglas de negocio
- Workflows en `citas-api/automations/n8n/`; credenciales externas; mínimo dato necesario.
## Dependencias y relaciones
- Épica: [[EP-010-automatizaciones-posteriores]]; depende de [[HU-025-consultar-auditoria-de-estados]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** agregación, agenda n8n y destinatarios externos.
## Tareas de desarrollo
- [ ] **T-01 — Definir métrica/fuente.** Dificultad: Medio. Sede/estado, periodo y visibilidad autorizada.
- [ ] **T-02 — Configurar/exportar resumen.** Dificultad: Medio. Workflow sin secretos.
- [ ] **T-03 — Validar resultado sintético.** Dificultad: Medio. Evidencia de agregación/envío.
## Criterios de aceptación
### CA-01 — Agregación diaria
**Dado** datos de citas sintéticos **Cuando** corre el flujo diario configurado **Entonces** genera un resumen agrupado por sede y estado.
### CA-02 — Artefacto seguro
**Dado** el workflow a versionar **Cuando** se inspecciona **Entonces** está exportado en la ubicación requerida sin secretos ni información real.
## Definition of Done
- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] JSON n8n, fuente/periodo aprobados, prueba sintética y trazabilidad verificadas.
- [ ] No se cambió el núcleo funcional de citas.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
## Notas y decisiones
- Definir hora, destinatarios y formato sin estimaciones temporales de sprint.
