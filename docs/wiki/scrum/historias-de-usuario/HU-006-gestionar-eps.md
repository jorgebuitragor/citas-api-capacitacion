---
id: HU-006
tipo: historia-de-usuario
titulo: "Gestionar EPS"
estado: Pendiente de aprobación
epica: "[[EP-002-catalogos-configurables]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 2"
dependencias: ["[[HU-003-iniciar-y-cerrar-sesion]]"]
relacionadas: ["[[HU-005-gestionar-perfil-y-afiliacion]]", "[[HU-007-gestionar-planes-eps]]"]
---
# HU-006 — Gestionar EPS
## Historia de usuario
**COMO** ADMIN **QUIERO** crear, consultar, actualizar y activar/desactivar EPS **PARA** mantener opciones de afiliación sin romper datos referenciados.
## Alcance
- CRUD autorizado y activación/desactivación.
## Fuera de alcance
- Borrado físico de EPS referenciada.
## Reglas de negocio
- Catálogo configurable referenciado se desactiva; datos sintéticos.
## Dependencias y relaciones
- Épica: [[EP-002-catalogos-configurables]]; depende de [[HU-003-iniciar-y-cerrar-sesion]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** autorización y protección de integridad referencial.
## Tareas de desarrollo
- [ ] **T-01 — Persistir catálogo y restricciones.** Dificultad: Medio. Migración/FK sin datos duplicados.
- [ ] **T-02 — Implementar gestión ADMIN.** Dificultad: Medio. Validación, autorización y errores contractuales.
- [ ] **T-03 — Integrar CRUD visual aprobado.** Dificultad: Bajo. Estados de lista/formulario.
## Criterios de aceptación
### CA-01 — Administración autorizada
**Dado** un ADMIN autenticado **Cuando** gestiona una EPS válida **Entonces** la creación/consulta/actualización se refleja según contrato.
### CA-02 — Protección de referencias
**Dado** una EPS usada en una afiliación o plan **Cuando** se intenta eliminar **Entonces** no se borra físicamente y se aplica desactivación cuando corresponda.
### CA-03 — Restricción de rol
**Dado** un rol distinto de ADMIN **Cuando** intenta gestionar EPS **Entonces** el servidor rechaza la operación.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración, integridad referencial, autorización y pruebas aplicables verificadas.
- [ ] Contrato/pantalla y trazabilidad Scrum actualizados.
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
- Unicidad nominal/código se define en contrato/modelo, no por inferencia.
