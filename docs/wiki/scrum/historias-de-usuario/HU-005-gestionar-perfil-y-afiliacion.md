---
id: HU-005
tipo: historia-de-usuario
titulo: "Gestionar perfil y afiliación"
estado: Pendiente de aprobación
epica: "[[EP-001-acceso-perfil-y-catalogos-fijos]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 1"
dependencias: ["[[HU-002-registrar-usuario]]", "[[HU-006-gestionar-eps]]", "[[HU-007-gestionar-planes-eps]]"]
relacionadas: []
---
# HU-005 — Gestionar perfil y afiliación
## Historia de usuario
**COMO** USER autenticado **QUIERO** consultar/actualizar los datos permitidos de mi perfil y asociar mi EPS, plan y régimen **PARA** mantener mi información de agendamiento vigente.
## Alcance
- Lectura/actualización permitida, afiliación por referencias a EPS/plan/régimen.
## Fuera de alcance
- Modificar identidad no autorizada o gestionar catálogos.
## Reglas de negocio
- Ownership; no repetir nombres de EPS/régimen/plan en usuario/cita; evitar duplicar afiliación.
## Dependencias y relaciones
- Épica: [[EP-001-acceso-perfil-y-catalogos-fijos]]; depende de [[HU-002-registrar-usuario]], [[HU-006-gestionar-eps]], [[HU-007-gestionar-planes-eps]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** relaciones 3FN, ownership y reglas de actualización.
## Tareas de desarrollo
- [ ] **T-01 — Modelar afiliación normalizada.** Dificultad: Alto. FK, cardinalidades e índices propios.
- [ ] **T-02 — Implementar casos de perfil.** Dificultad: Medio. Autorizar por ownership y validar referencias activas.
- [ ] **T-03 — Integrar pantalla de perfil.** Dificultad: Medio. Estados y validaciones de UX.
## Criterios de aceptación
### CA-01 — Perfil propio
**Dado** un USER autenticado **Cuando** consulta o cambia campos permitidos **Entonces** solo accede/modifica su perfil y recibe validación contractual.
### CA-02 — Afiliación válida
**Dado** EPS, plan y régimen válidos **Cuando** los asocio **Entonces** se guarda mediante referencias normalizadas sin duplicar esos datos.
### CA-03 — Referencia inválida
**Dado** una referencia inexistente o no disponible **Cuando** intento asociarla **Entonces** la operación se rechaza y la afiliación vigente permanece íntegra.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración/FK 3FN, ownership y pruebas de persistencia/REST aplicables verificadas.
- [ ] Pantalla web aprobada conserva datos seguros ante errores cuando exista.
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
- Depende de catálogos configurables aunque aparece en el primer incremento; puede implementarse perfil antes y afilación después si se aprueba dividir.
