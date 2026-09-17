---
id: HU-019
tipo: historia-de-usuario
titulo: "Cancelar cita"
estado: Pendiente de aprobación
epica: "[[EP-006-gestion-de-citas-del-usuario]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-018-consultar-mis-citas]]"]
relacionadas: ["[[HU-025-consultar-auditoria-de-estados]]"]
---
# HU-019 — Cancelar cita
## Historia de usuario
**COMO** USER **QUIERO** cancelar una cita futura no terminal propia **PARA** liberar el horario que ya no usaré.
## Alcance
- Transición a `CANCELLED`, liberación de slots e historial.
## Fuera de alcance
- Reactivar directamente una cancelada.
## Reglas de negocio
- RN-09 y RN-11; solo futura/no terminal; no reactivación directa.
## Dependencias y relaciones
- Épica: [[EP-006-gestion-de-citas-del-usuario]]; depende de [[HU-018-consultar-mis-citas]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** autorización, estado, slots y auditoría atómicos.
## Tareas de desarrollo
- [ ] **T-01 — Definir transición de cancelación.** Dificultad: Alto. Validar ownership/futuro/no terminal.
- [ ] **T-02 — Liberar reserva e historial.** Dificultad: Alto. Operación consistente y pruebas.
- [ ] **T-03 — Integrar confirmación UX.** Dificultad: Medio. Evitar doble acción.
## Criterios de aceptación
### CA-01 — Cancelación permitida
**Dado** una cita propia futura no terminal **Cuando** USER la cancela **Entonces** queda `CANCELLED`, se liberan slots y se registra historial.
### CA-02 — Cancelación no permitida
**Dado** una cita pasada, terminal o ajena **Cuando** USER intenta cancelarla **Entonces** la operación se rechaza sin cambios.
### CA-03 — Sin reactivación directa
**Dado** una cita `CANCELLED` **Cuando** intento reactivarla **Entonces** el flujo no la restaura directamente.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Transición, liberación, auditoría, ownership y pruebas integración verificadas.
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
- No se definió una ventana adicional de cancelación; no debe inventarse.
