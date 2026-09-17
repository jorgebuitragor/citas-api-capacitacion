---
id: HU-022
tipo: historia-de-usuario
titulo: "Consultar agenda profesional"
estado: Pendiente de aprobación
epica: "[[EP-007-operacion-clinica-del-profesional]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-015-reservar-cita-general]]", "[[HU-017-decidir-cita-especializada]]"]
relacionadas: ["[[HU-023-cerrar-atencion]]"]
---
# HU-022 — Consultar agenda profesional
## Historia de usuario
**COMO** PROFESSIONAL **QUIERO** ver mis citas `APPROVED` por día/semana y sede **PARA** preparar mi atención sin consultar datos ajenos.
## Alcance
- Agenda propia filtrable por periodo/sede con citas aprobadas.
## Fuera de alcance
- Citas ajenas e historia clínica.
## Reglas de negocio
- Ownership; solo `APPROVED`; mínima exposición de datos necesaria para propia cita.
## Dependencias y relaciones
- Épica: [[EP-007-operacion-clinica-del-profesional]]; depende de [[HU-015-reservar-cita-general]], [[HU-017-decidir-cita-especializada]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** filtros, ownership y proyección segura.
## Tareas de desarrollo
- [ ] **T-01 — Definir consulta propia.** Dificultad: Medio. Filtros y contrato.
- [ ] **T-02 — Proteger datos/ownership.** Dificultad: Medio. Pruebas negativas.
- [ ] **T-03 — Integrar calendario agenda.** Dificultad: Medio. Estados y accesibilidad.
## Criterios de aceptación
### CA-01 — Agenda filtrada
**Dado** un PROFESSIONAL con citas aprobadas **Cuando** selecciona día/semana y sede **Entonces** ve solo sus citas `APPROVED` en ese criterio.
### CA-02 — Sin datos ajenos
**Dado** otro profesional/usuario **Cuando** intento consultar su agenda **Entonces** el acceso es denegado.
## Definition of Done
- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] Ownership, proyección de datos, contrato, pruebas y UI aplicables verificadas.
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
- Campos exactos expuestos se pactan en contrato con minimización de datos.
