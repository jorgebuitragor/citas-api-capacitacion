---
id: HU-002
tipo: historia-de-usuario
titulo: "Registrar usuario"
estado: Aprobada
epica: "[[EP-001-acceso-perfil-y-catalogos-fijos]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 1"
dependencias: ["[[HU-001-consultar-catalogos-fijos]]"]
relacionadas: ["[[HU-003-iniciar-y-cerrar-sesion]]"]
---
# HU-002 — Registrar usuario
## Historia de usuario
**COMO** visitante **QUIERO** crear una cuenta USER con mis datos mínimos **PARA** acceder al agendamiento ficticio.
## Alcance
- Nombres, apellidos, tipo/número de documento, email, teléfono y contraseña; asignación de rol USER.
## Fuera de alcance
- Alta de PROFESSIONAL por autoservicio y afiliación.
## Reglas de negocio
- Email y documento son únicos; password jamás se persiste ni registra en texto plano.
## Dependencias y relaciones
- Épica: [[EP-001-acceso-perfil-y-catalogos-fijos]]; depende de [[HU-001-consultar-catalogos-fijos]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** identidad, seguridad, unicidad, persistencia y experiencia de formulario.
## Tareas de desarrollo
- [ ] **T-01 — Modelar identidad y restricciones.** Dificultad: Alto. Migración, hashes y restricciones únicas 3FN.
- [ ] **T-02 — Implementar caso de uso y borde REST.** Dificultad: Alto. Validación server-side sin logging sensible.
- [ ] **T-03 — Integrar formulario aprobado.** Dificultad: Medio. Estados loading/error/success sin doble envío.
## Criterios de aceptación
### CA-01 — Registro válido
**Dado** datos mínimos válidos y sintéticos **Cuando** el visitante se registra **Entonces** se crea una cuenta USER y se confirma según el contrato.
### CA-02 — Unicidad
**Dado** un email o documento ya registrado **Cuando** se intenta registrar otra cuenta **Entonces** se rechaza sin crear una cuenta duplicada.
### CA-03 — Protección de contraseña
**Dado** un registro exitoso **Cuando** se revisa la persistencia/logs relevantes **Entonces** la contraseña no está expuesta en texto plano.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración, hash adaptativo, validaciones y pruebas dominio/REST/persistencia aplicables verificados.
- [ ] Pantalla aprobada maneja errores y accesibilidad básica cuando exista frontend.
- [ ] Contrato y trazabilidad Scrum actualizados.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| CA-03 | Pendiente | — | — |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
- 2026-09-17 — Aprobada explícitamente por el usuario para selección e implementación posterior.
## Notas y decisiones
- Reglas de formato de documento/email deben formalizarse sin introducir PII real.
