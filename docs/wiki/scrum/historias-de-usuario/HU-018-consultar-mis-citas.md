---
id: HU-018
tipo: historia-de-usuario
titulo: "Consultar mis citas"
estado: Pendiente de aprobación
epica: "[[EP-006-gestion-de-citas-del-usuario]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 4"
dependencias: ["[[HU-015-reservar-cita-general]]", "[[HU-016-solicitar-cita-especializada]]"]
relacionadas: ["[[HU-019-cancelar-cita]]", "[[HU-020-solicitar-reprogramacion]]"]
---
# HU-018 — Consultar mis citas
## Historia de usuario
**COMO** USER **QUIERO** listar y filtrar mis citas por estado/fecha y ver su detalle **PARA** gestionar mis atenciones.
## Alcance
- Mostrar sede, profesional, especialidad, fecha/hora, duración, estado y motivo de rechazo cuando exista.
## Fuera de alcance
- Consultar citas de otros usuarios.
## Reglas de negocio
- Ownership; no repetir datos de catálogo en la cita si existe FK; se presenta el dato relacionado.
## Dependencias y relaciones
- Épica: [[EP-006-gestion-de-citas-del-usuario]]; depende de [[HU-015-reservar-cita-general]], [[HU-016-solicitar-cita-especializada]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** proyección filtrada, ownership y estados de UI.
## Tareas de desarrollo
- [ ] **T-01 — Definir consulta propia.** Dificultad: Medio. Filtros, orden y contrato.
- [ ] **T-02 — Aplicar ownership.** Dificultad: Medio. Pruebas de acceso indebido.
- [ ] **T-03 — Implementar listado/detalle.** Dificultad: Medio. Loading, empty, error y accesibilidad.
## Criterios de aceptación
### CA-01 — Información mínima
**Dado** citas propias **Cuando** USER las consulta **Entonces** ve todos los campos mínimos y el motivo de rechazo cuando corresponde.
### CA-02 — Filtros
**Dado** citas en distintas fechas/estados **Cuando** filtra por estado o fecha **Entonces** el resultado respeta los filtros.
### CA-03 — Ownership
**Dado** una cita de otro USER **Cuando** intento consultarla **Entonces** no se expone.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Contrato, ownership, pruebas y estados de interfaz aplicables verificados.
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
- Paginar/ordenar requiere contrato, no está exigido por PRD.
