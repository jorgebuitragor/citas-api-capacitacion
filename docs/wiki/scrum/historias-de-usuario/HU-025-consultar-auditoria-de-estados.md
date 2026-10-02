---
id: HU-025
tipo: historia-de-usuario
titulo: "Consultar auditoría de estados"
estado: Completada
epica: "[[EP-008-operacion-administrativa-y-trazabilidad]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 5"
dependencias: ["[[HU-015-reservar-cita-general]]", "[[HU-017-decidir-cita-especializada]]"]
relacionadas: ["[[HU-019-cancelar-cita]]", "[[HU-021-decidir-reprogramacion]]", "[[HU-023-cerrar-atencion]]"]
---
# HU-025 — Consultar auditoría de estados
## Historia de usuario
**COMO** actor autorizado **QUIERO** consultar el historial de estados pertinente de una cita **PARA** verificar cómo cambió y quién/qué lo originó.
## Alcance
- Eventos con cita, estado nuevo, actor cuando existe, fuente SYSTEM/USER/ADMIN, fecha/hora y motivo opcional.
## Fuera de alcance
- CRUD/edición de auditoría y acceso indiscriminado.
## Reglas de negocio
- RN-12: la auditoría no se modifica como CRUD normal; authorization/ownership según rol y contrato.
## Dependencias y relaciones
- Épica: [[EP-008-operacion-administrativa-y-trazabilidad]]; depende de [[HU-015-reservar-cita-general]], [[HU-017-decidir-cita-especializada]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** inmutabilidad, proyección y autorización por actor.
## Tareas de desarrollo
- [x] **T-01 — Modelar eventos inmutables.** Dificultad: Medio. Migración 3FN y fuente/actor/motivo.
- [x] **T-02 — Registrar transiciones.** Dificultad: Alto. Integrar casos de uso sin CRUD normal.
- [x] **T-03 — Publicar consulta autorizada.** Dificultad: Medio. Contrato/pruebas de visibilidad.
## Criterios de aceptación
### CA-01 — Evento completo
**Dado** una transición de cita **Cuando** se consulta su historial autorizado **Entonces** se ve estado nuevo, actor aplicable, fuente, fecha/hora y motivo aplicable.
### CA-02 — Inmutabilidad
**Dado** un evento creado **Cuando** se intenta editar/eliminar como CRUD normal **Entonces** la operación no está disponible/permitida.
### CA-03 — Visibilidad controlada
**Dado** una cita ajena o rol no habilitado **Cuando** se intenta consultar sus eventos **Entonces** no se exponen.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Migración, inmutabilidad, autorización, pruebas y contrato aplicables verificados.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `BookingIntegrationTest.statusHistoryVisibleToAdminOwnerUserAndOwnerProfessionalButDeniedToOthers`; verificación manual en navegador real | `GET /api/v1/appointments/{id}/status-history` devuelve estado, actor (`null` si `SYSTEM`), fuente, `changedAt` y motivo. |
| CA-02 | Cumple | `BookingIntegrationTest.statusHistoryHasNoCrudEndpoints` (`POST` al mismo path → `405`) | No existen `POST`/`PUT`/`PATCH`/`DELETE` para `appointment_status_history`; se escribe únicamente como efecto de transiciones. |
| CA-03 | Cumple | Mismo test de visibilidad (USER y PROFESSIONAL ajenos → `404`; cita inexistente → `404`); verificación manual con ADMIN y USER propietario | Autorización por ownership en `BookingService.statusHistory`: ADMIN cualquiera, USER su propia cita, PROFESSIONAL las que atendió. |
| DoD | Cumple | `docs/contracts/appointments.md`; backend 29/29; frontend typecheck, Vitest 29/29 y build; `docs/evidence/goals-loops/S4/HU-024-HU-025-implementation.md` | Sin migración nueva: `appointment_status_history` ya tenía todas las columnas necesarias desde V3 (DEC-001). |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-10-02 — DEC-007: el usuario aprueba la matriz de visibilidad por ownership (ya borrador en el contrato), cerrando la pregunta abierta sobre la matriz de lectura por rol.
- 2026-10-02 — Validada y completada con evidencia de implementación y verificaciones backend/frontend registradas en `docs/evidence/goals-loops/S4/HU-024-HU-025-implementation.md`.
## Notas y decisiones
- Matriz de lectura por rol resuelta en DEC-007: ownership (ADMIN cualquiera, USER propia, PROFESSIONAL atendida), no restringida solo a ADMIN.
- No se verificó manualmente en navegador el acceso del rol PROFESSIONAL por no contar con credenciales reales de las cuentas semilla `prof.*@demo.invalid`; cubierto por prueba de integración y revisión de código.
