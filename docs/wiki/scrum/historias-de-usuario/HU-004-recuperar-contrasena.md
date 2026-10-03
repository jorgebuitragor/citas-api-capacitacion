---
id: HU-004
tipo: historia-de-usuario
titulo: "Recuperar contraseña"
estado: Completada
epica: "[[EP-001-acceso-perfil-y-catalogos-fijos]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 1"
dependencias: ["[[HU-002-registrar-usuario]]"]
relacionadas: ["[[HU-003-iniciar-y-cerrar-sesion]]"]
---
# HU-004 — Recuperar contraseña
## Historia de usuario
**COMO** usuario registrado **QUIERO** solicitar y completar la recuperación de mi contraseña **PARA** recuperar el acceso de forma segura.
## Alcance
- Solicitud por email, token temporal de un uso y cambio de contraseña.
## Fuera de alcance
- SMTP obligatorio; puede usarse exposición controlada de desarrollo según PRD.
## Reglas de negocio
- Token temporal/único; cambio consume/invalida token; no exponer datos sensibles.
## Dependencias y relaciones
- Épica: [[EP-001-acceso-perfil-y-catalogos-fijos]]; depende de [[HU-002-registrar-usuario]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** token seguro, expiración pendiente y flujo de dos pasos.
## Tareas de desarrollo
- [x] **T-01 — Modelar token de reset.** Dificultad: Medio. Persistencia, consumo único y expiración configurable.
- [x] **T-02 — Exponer solicitud/cambio seguro.** Dificultad: Medio. Validar borde y evitar filtración de cuentas.
- [x] **T-03 — Integrar vistas aprobadas.** Dificultad: Bajo. Estados de formulario y mensajes seguros.
## Criterios de aceptación
### CA-01 — Solicitud controlada
**Dado** un email registrado **Cuando** solicito recuperación **Entonces** se crea un token temporal de un uso y se entrega por el canal autorizado.
### CA-02 — Cambio con token válido
**Dado** un token vigente no consumido **Cuando** establezco una contraseña válida **Entonces** la contraseña cambia con hash y el token queda inutilizable.
### CA-03 — Token no reutilizable
**Dado** un token vencido, consumido o inválido **Cuando** intento usarlo **Entonces** el cambio se rechaza.
## Definition of Done
- [x] CA-01 a CA-03 validados con evidencia.
- [x] Migración y pruebas de token único/expiración aplicables verificadas.
- [x] Ningún password/token queda registrado o expuesto fuera del modo de desarrollo autorizado.
- [x] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Cumple | `AuthIntegrationTest.requestingResetForActiveEmailIssuesSingleUseTokenOnlyInLogNeverInResponse`; verificación manual y E2E | `202` siempre; el token solo existe en el log del servidor (DEC-008), nunca en la respuesta HTTP; expira a los 15 min. |
| CA-02 | Cumple | Mismo test; E2E del 2026-10-03 | Contraseña cambia con BCrypt; login con la anterior `401`, con la nueva `200`; el token queda consumido. |
| CA-03 | Cumple | `AuthIntegrationTest.confirmRejectsExpiredOrUnknownToken`; reuso del token `401` en E2E | Token vencido, consumido o inexistente responde `401` genérico sin distinguir el motivo. |
| DoD | Cumple | `docs/contracts/authentication.md`; backend 43/43; frontend 39/39; `docs/evidence/goals-loops/S4/HU-004-implementation.md` | Migración V8; ningún password/token fuera del canal de log autorizado. |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-10-03 — DEC-008: el usuario aprueba duración (15 min), canal (log del servidor) y contrato REST.
- 2026-10-03 — Implementada y verificada (backend, frontend, manual y E2E); evidencia en `docs/evidence/goals-loops/S4/HU-004-implementation.md`.
- 2026-10-03 — El usuario instruye marcar formalmente `Completada` (excepción puntual a `AGENTS.md`, registrada en `decisiones.md`).
## Notas y decisiones
- Duración y canal exactos son pregunta abierta.
- Duración y canal resueltos en DEC-008: token de 15 minutos, canal log del servidor.

