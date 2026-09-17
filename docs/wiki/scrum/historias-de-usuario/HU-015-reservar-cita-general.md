---
id: HU-015
tipo: historia-de-usuario
titulo: "Reservar cita general"
estado: Pendiente de aprobación
epica: "[[EP-005-descubrimiento-y-solicitud-de-citas]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 3"
dependencias: ["[[HU-014-consultar-disponibilidad-para-reserva]]"]
relacionadas: ["[[HU-018-consultar-mis-citas]]", "[[HU-025-consultar-auditoria-de-estados]]"]
---
# HU-015 — Reservar cita general
## Historia de usuario
**COMO** USER **QUIERO** confirmar una franja disponible de Medicina General con un profesional general **PARA** obtener una cita aprobada de inmediato.
## Alcance
- Selección y confirmación de cita general disponible; estado `APPROVED` automático.
## Fuera de alcance
- Aprobación ADMIN o reserva especializada.
## Reglas de negocio
- RN-01, RN-02, RN-06, RN-08; no doble reserva y transición auditada.
## Dependencias y relaciones
- Épica: [[EP-005-descubrimiento-y-solicitud-de-citas]]; depende de [[HU-014-consultar-disponibilidad-para-reserva]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** operación transaccional/concurrente, estado y auditoría.
## Tareas de desarrollo
- [ ] **T-01 — Modelar cita/ocupación/auditoría.** Dificultad: Alto. Migración 3FN e índices.
- [ ] **T-02 — Ejecutar reserva atómica.** Dificultad: Alto. Comprobar disponibilidad al confirmar y prevenir carreras.
- [ ] **T-03 — Integrar confirmación.** Dificultad: Medio. Evitar doble envío y comunicar conflicto.
## Criterios de aceptación
### CA-01 — Aprobación automática
**Dado** una franja general aún disponible **Cuando** USER confirma **Entonces** se crea una cita `APPROVED` con profesional general y slots ocupados.
### CA-02 — Conflicto al confirmar
**Dado** que otro proceso ocupó la franja antes de confirmar **Cuando** USER intenta reservar **Entonces** no se crea cita duplicada y recibe respuesta contractual de conflicto.
### CA-03 — Auditoría
**Dado** una reserva exitosa **Cuando** se revisa su historial **Entonces** existe evento de estado con fuente/fecha/hora y actor aplicable.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración, atomicidad/concurrencia aprobada, pruebas integración y contrato consumido verificadas.
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
- Estrategia exacta de bloqueo/optimismo no está aprobada.
