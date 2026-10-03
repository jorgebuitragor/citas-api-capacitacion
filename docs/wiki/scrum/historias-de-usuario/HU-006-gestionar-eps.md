---
id: HU-006
tipo: historia-de-usuario
titulo: "Gestionar EPS"
estado: Completada
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
- [x] **T-01 — Persistir catálogo y restricciones.** Dificultad: Medio. Migración/FK sin datos duplicados.
- [x] **T-02 — Implementar gestión ADMIN.** Dificultad: Medio. Validación, autorización y errores contractuales.
- [x] **T-03 — Integrar CRUD visual aprobado.** Dificultad: Bajo. Estados de lista/formulario.
## Criterios de aceptación
### CA-01 — Administración autorizada
**Dado** un ADMIN autenticado **Cuando** gestiona una EPS válida **Entonces** la creación/consulta/actualización se refleja según contrato.
### CA-02 — Protección de referencias
**Dado** una EPS usada en una afiliación o plan **Cuando** se intenta eliminar **Entonces** no se borra físicamente y se aplica desactivación cuando corresponda.
### CA-03 — Restricción de rol
**Dado** un rol distinto de ADMIN **Cuando** intenta gestionar EPS **Entonces** el servidor rechaza la operación.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Migración, integridad referencial, autorización y pruebas aplicables verificadas.
- [x] Contrato/pantalla y trazabilidad Scrum actualizados.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `CatalogIntegrationTest.createsEpsListsItIncludingInactiveAndRejectsDuplicateCode`; verificación manual y E2E | ADMIN crea, consulta (incluye inactivas) y actualiza EPS; código duplicado `409`. |
| CA-02 | Cumple | No existe endpoint de borrado; solo `active` en el contrato (`docs/contracts/catalogs.md`) | Sin borrado físico; se desactiva (RF-06). |
| CA-03 | Cumple | `CatalogIntegrationTest.epsManagementRequiresAdminRole` | Rol no ADMIN `403`; sin autenticación `401`. |
| DoD | Cumple | `docs/contracts/catalogs.md`; backend 43/43; frontend 39/39; `docs/evidence/goals-loops/S4/HU-006-HU-007-HU-008-implementation.md` | Migración V9 tomada del esquema de referencia (DEC-001/DEC-009). |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-10-03 — DEC-009: el usuario aprueba el plan y el contrato; unicidad por `code` resuelta con el esquema de referencia (DEC-001).
- 2026-10-03 — Implementada y verificada (backend, frontend, manual y E2E); evidencia en `docs/evidence/goals-loops/S4/HU-006-HU-007-HU-008-implementation.md`.
- 2026-10-03 — El usuario instruye marcar formalmente `Completada` (excepción puntual a `AGENTS.md`, registrada en `decisiones.md`).
## Notas y decisiones
- Unicidad nominal/código se define en contrato/modelo, no por inferencia.
- Unicidad resuelta en DEC-009: `eps.code` único (no el nombre), según `database/reference/db.sql`.

