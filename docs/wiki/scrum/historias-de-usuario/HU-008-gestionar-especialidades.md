---
id: HU-008
tipo: historia-de-usuario
titulo: "Gestionar especialidades"
estado: Completada
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
- [x] **T-01 — Modelar especialidad/duración.** Dificultad: Medio. Migración, restricciones y catálogo activo.
- [x] **T-02 — Implementar gestión ADMIN.** Dificultad: Medio. Validación y preservación de referencias.
- [x] **T-03 — Propagar duración al cálculo de disponibilidad.** Dificultad: Alto. Contrato y pruebas cruzadas.
## Criterios de aceptación
### CA-01 — Duración válida
**Dado** un ADMIN **Cuando** crea/actualiza una especialidad **Entonces** solo puede definir 30 o 60 minutos.
### CA-02 — Uso en reserva
**Dado** una especialidad activa asociada al profesional **Cuando** se consulta disponibilidad **Entonces** su duración determina uno o dos slots consecutivos requeridos.
### CA-03 — Catálogo referenciado
**Dado** una especialidad usada **Cuando** se intenta eliminar **Entonces** no se borra físicamente; se aplica desactivación cuando proceda.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Migración/FK, pruebas de duración 30/60, autorización y contrato verificados.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `CatalogIntegrationTest.createsSpecialtyValidatesDurationAndKeepsCodeAndDurationImmutable`; E2E | Solo 30 o 60 minutos (`400` para otro valor, p. ej. 45); código/duración inmutables tras crear. |
| CA-02 | Cumple | Cobertura previa de HU-014/015/016 (`BookingIntegrationTest`) con la tabla `specialties` existente | La duración 30/60 ya determina uno o dos slots consecutivos; este incremento no cambió ese cálculo. |
| CA-03 | Cumple | No existe endpoint de borrado; solo `active`; E2E (inactiva no aparece en el catálogo de reserva) | Se desactiva, no se borra (RF-06). |
| DoD | Cumple | `docs/contracts/catalogs.md`; backend 43/43; frontend 39/39; `docs/evidence/goals-loops/S4/HU-006-HU-007-HU-008-implementation.md` | Sin migración nueva: `specialties` ya existía desde V3. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-10-03 — DEC-009: el usuario aprueba el plan y el contrato.
- 2026-10-03 — Implementada y verificada (backend, frontend, manual y E2E); evidencia en `docs/evidence/goals-loops/S4/HU-006-HU-007-HU-008-implementation.md`.
- 2026-10-03 — El usuario instruye marcar formalmente `Completada` (excepción puntual a `AGENTS.md`, registrada en `decisiones.md`).
## Notas y decisiones
- Medicina General debe quedar disponible según catálogo/semilla aprobado, sin inventar ID.
- Medicina General se reutiliza del seed ya existente (V3), sin inventar ID.

