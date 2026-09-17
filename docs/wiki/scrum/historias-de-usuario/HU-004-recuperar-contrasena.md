---
id: HU-004
tipo: historia-de-usuario
titulo: "Recuperar contraseña"
estado: Pendiente de aprobación
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
- [ ] **T-01 — Modelar token de reset.** Dificultad: Medio. Persistencia, consumo único y expiración configurable.
- [ ] **T-02 — Exponer solicitud/cambio seguro.** Dificultad: Medio. Validar borde y evitar filtración de cuentas.
- [ ] **T-03 — Integrar vistas aprobadas.** Dificultad: Bajo. Estados de formulario y mensajes seguros.
## Criterios de aceptación
### CA-01 — Solicitud controlada
**Dado** un email registrado **Cuando** solicito recuperación **Entonces** se crea un token temporal de un uso y se entrega por el canal autorizado.
### CA-02 — Cambio con token válido
**Dado** un token vigente no consumido **Cuando** establezco una contraseña válida **Entonces** la contraseña cambia con hash y el token queda inutilizable.
### CA-03 — Token no reutilizable
**Dado** un token vencido, consumido o inválido **Cuando** intento usarlo **Entonces** el cambio se rechaza.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración y pruebas de token único/expiración aplicables verificadas.
- [ ] Ningún password/token queda registrado o expuesto fuera del modo de desarrollo autorizado.
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
- Duración y canal exactos son pregunta abierta.
