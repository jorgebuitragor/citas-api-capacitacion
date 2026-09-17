---
id: HU-024
tipo: historia-de-usuario
titulo: "Consultar bandeja administrativa"
estado: Pendiente de aprobación
epica: "[[EP-008-operacion-administrativa-y-trazabilidad]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 5"
dependencias: ["[[HU-016-solicitar-cita-especializada]]", "[[HU-020-solicitar-reprogramacion]]"]
relacionadas: ["[[HU-017-decidir-cita-especializada]]", "[[HU-021-decidir-reprogramacion]]"]
---
# HU-024 — Consultar bandeja administrativa
## Historia de usuario
**COMO** ADMIN **QUIERO** ver citas especializadas `REQUESTED` y reprogramaciones `PENDING` filtradas **PARA** resolver las solicitudes pendientes.
## Alcance
- Bandeja y filtros sede, profesional, especialidad y fecha.
## Fuera de alcance
- Decidir una solicitud desde esta HU.
## Reglas de negocio
- Solo ADMIN; estados exactos de pendientes.
## Dependencias y relaciones
- Épica: [[EP-008-operacion-administrativa-y-trazabilidad]]; depende de [[HU-016-solicitar-cita-especializada]], [[HU-020-solicitar-reprogramacion]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** proyección de dos tipos de solicitud y filtros.
## Tareas de desarrollo
- [ ] **T-01 — Diseñar consultas de bandeja.** Dificultad: Medio. Filtros e índices.
- [ ] **T-02 — Proteger acceso ADMIN.** Dificultad: Medio. Pruebas de rol.
- [ ] **T-03 — Renderizar pendientes.** Dificultad: Medio. Estados y enlaces de decisión.
## Criterios de aceptación
### CA-01 — Pendientes correctos
**Dado** solicitudes existentes **Cuando** ADMIN abre la bandeja **Entonces** ve especializadas `REQUESTED` y reprogramaciones `PENDING`.
### CA-02 — Filtros
**Dado** solicitudes variadas **Cuando** aplica los filtros exigidos **Entonces** el resultado se ajusta a ellos.
### CA-03 — Rol
**Dado** un rol no ADMIN **Cuando** accede a la bandeja **Entonces** el servidor lo deniega.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Filtros, autorización, contrato, pruebas y estados web aplicables verificados.
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
- Orden/paginación queda para el contrato.
