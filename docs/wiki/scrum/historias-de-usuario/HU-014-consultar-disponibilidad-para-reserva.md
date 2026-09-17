---
id: HU-014
tipo: historia-de-usuario
titulo: "Consultar disponibilidad para reserva"
estado: Pendiente de aprobación
epica: "[[EP-005-descubrimiento-y-solicitud-de-citas]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 3"
dependencias: ["[[HU-008-gestionar-especialidades]]", "[[HU-010-asignar-especialidades-al-profesional]]", "[[HU-012-gestionar-bloques-de-disponibilidad]]"]
relacionadas: ["[[HU-015-reservar-cita-general]]", "[[HU-016-solicitar-cita-especializada]]"]
---
# HU-014 — Consultar disponibilidad para reserva
## Historia de usuario
**COMO** USER **QUIERO** filtrar horarios por sede, tipo de cita, especialidad, profesional y fecha **PARA** elegir una franja que complete la atención requerida.
## Alcance
- Filtros solicitados y resultado de slots reservables de 30/60 min.
## Fuera de alcance
- Crear la reserva o mostrar slots no completables.
## Reglas de negocio
- Especialidad activa/asociada; 60 min requiere dos slots consecutivos; no se muestran retenidos/reservados.
## Dependencias y relaciones
- Épica: [[EP-005-descubrimiento-y-solicitud-de-citas]]; depende de [[HU-008-gestionar-especialidades]], [[HU-010-asignar-especialidades-al-profesional]], [[HU-012-gestionar-bloques-de-disponibilidad]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** filtros y cálculo de slots con consistencia frente a reservas.
## Tareas de desarrollo
- [ ] **T-01 — Diseñar consulta de disponibilidad.** Dificultad: Alto. Índices/puertos, filtros y duración.
- [ ] **T-02 — Excluir conflictos.** Dificultad: Alto. Aplicar reservas y retenciones aprobadas.
- [ ] **T-03 — Integrar búsqueda aprobada.** Dificultad: Medio. Estados loading/empty/error.
## Criterios de aceptación
### CA-01 — Filtros
**Dado** disponibilidad publicada **Cuando** USER aplica los filtros definidos **Entonces** recibe resultados coherentes con ellos.
### CA-02 — Duración completa
**Dado** una especialidad de 30 o 60 minutos **Cuando** se muestran horarios **Entonces** solo aparecen franjas de uno o dos slots consecutivos libres, respectivamente.
### CA-03 — Exclusión de conflicto
**Dado** slots reservados o retenidos **Cuando** se consulta disponibilidad **Entonces** no se ofrecen para una nueva reserva.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Pruebas de filtros, duración, conflictos y contrato frontend-backend aplicables verificadas.
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
- Definir concurrencia y vida de retención antes de aprobar implementación.
