---
id: HU-008
tipo: historia-de-usuario
titulo: "Gestionar especialidades"
estado: Pendiente de aprobación
epica: "[[EP-002-catalogos-configurables]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 2"
dependencias: ["[[HU-003-iniciar-y-cerrar-sesion]]"]
relacionadas: ["[[HU-010-asignar-especialidades-al-profesional]]", "[[HU-014-consultar-disponibilidad-para-reserva]]"]
---
# HU-008 — Gestionar especialidades
## Historia de usuario
**COMO** ADMIN **QUIERO** administrar especialidades y su duración de 30 o 60 minutos **PARA** definir la oferta y duración correcta de las citas.
## Alcance
- CRUD/activación autorizado; duración discreta de 30/60 minutos.
## Fuera de alcance
- El profesional no modifica la duración.
## Reglas de negocio
- Especialidad debe estar activa y asociada al profesional para reservarse; 60 min exige dos slots consecutivos.
## Dependencias y relaciones
- Épica: [[EP-002-catalogos-configurables]]; relacionada: [[HU-010-asignar-especialidades-al-profesional]], [[HU-014-consultar-disponibilidad-para-reserva]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** regla de duración impacta agenda, disponibilidad y reserva.
## Tareas de desarrollo
- [ ] **T-01 — Modelar especialidad/duración.** Dificultad: Medio. Migración, restricciones y catálogo activo.
- [ ] **T-02 — Implementar gestión ADMIN.** Dificultad: Medio. Validación y preservación de referencias.
- [ ] **T-03 — Propagar duración al cálculo de disponibilidad.** Dificultad: Alto. Contrato y pruebas cruzadas.
## Criterios de aceptación
### CA-01 — Duración válida
**Dado** un ADMIN **Cuando** crea/actualiza una especialidad **Entonces** solo puede definir 30 o 60 minutos.
### CA-02 — Uso en reserva
**Dado** una especialidad activa asociada al profesional **Cuando** se consulta disponibilidad **Entonces** su duración determina uno o dos slots consecutivos requeridos.
### CA-03 — Catálogo referenciado
**Dado** una especialidad usada **Cuando** se intenta eliminar **Entonces** no se borra físicamente; se aplica desactivación cuando proceda.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración/FK, pruebas de duración 30/60, autorización y contrato verificados.
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
- Medicina General debe quedar disponible según catálogo/semilla aprobado, sin inventar ID.
