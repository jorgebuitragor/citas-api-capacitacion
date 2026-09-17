---
tipo: indice-scrum
estado: Pendiente de aprobación
---

# Mapa Scrum / Spec-Driven Development — Sistema ficticio de citas

Este mapa traduce exclusivamente `PRD.md`, `RESTRICCIONES_TECNICAS.md` y `database/REQUISITOS_NORMALIZACION_3FN.md`. No autoriza implementación fuera de las HU explícitamente aprobadas.

## HU aprobadas para selección

- [[HU-002-registrar-usuario]]
- [[HU-003-iniciar-y-cerrar-sesion]]
- [[HU-026-documentar-contrato-rest-base]]

Las demás HU permanecen en `Pendiente de aprobación`.

## Contexto técnico constatado

- Backend previsto: Java 21, Spring Boot 3.5.x, Maven, arquitectura hexagonal, Spring Data JPA, MySQL 8.4, Flyway y Spring Security con JWT access/refresh.
- Frontend previsto: TypeScript; React o Angular se determinará después del flujo Stitch/Google AI Studio. Consumo REST directo, sin BFF/Express.
- Repositorios inicialmente vacíos: aún no hay contrato REST, modelo SQL propio ni diseño visual aprobado.

## Épicas propuestas

- [[EP-001-acceso-perfil-y-catalogos-fijos]]
- [[EP-002-catalogos-configurables]]
- [[EP-003-administracion-de-profesionales]]
- [[EP-004-disponibilidad-del-profesional]]
- [[EP-005-descubrimiento-y-solicitud-de-citas]]
- [[EP-006-gestion-de-citas-del-usuario]]
- [[EP-007-operacion-clinica-del-profesional]]
- [[EP-008-operacion-administrativa-y-trazabilidad]]
- [[EP-009-contrato-rest-y-experiencia-web]]
- [[EP-010-automatizaciones-posteriores]]

## Incrementos / sprints sugeridos

No son estimaciones de tiempo ni capacidad; el orden reduce dependencias para una sola persona.

1. **Incremento 1 — Acceso y base de datos verificable:** [[HU-001-consultar-catalogos-fijos]], [[HU-002-registrar-usuario]], [[HU-003-iniciar-y-cerrar-sesion]], [[HU-004-recuperar-contrasena]], [[HU-005-gestionar-perfil-y-afiliacion]], [[HU-026-documentar-contrato-rest-base]].
2. **Incremento 2 — Oferta administrativa:** [[HU-006-gestionar-eps]], [[HU-007-gestionar-planes-eps]], [[HU-008-gestionar-especialidades]], [[HU-009-crear-y-administrar-profesional]], [[HU-010-asignar-especialidades-al-profesional]], [[HU-011-asignar-sedes-al-profesional]].
3. **Incremento 3 — Agenda y reserva:** [[HU-012-gestionar-bloques-de-disponibilidad]], [[HU-013-consultar-calendario-de-disponibilidad]], [[HU-014-consultar-disponibilidad-para-reserva]], [[HU-015-reservar-cita-general]], [[HU-016-solicitar-cita-especializada]], [[HU-017-decidir-cita-especializada]].
4. **Incremento 4 — Ciclo de vida de la cita:** [[HU-018-consultar-mis-citas]], [[HU-019-cancelar-cita]], [[HU-020-solicitar-reprogramacion]], [[HU-021-decidir-reprogramacion]], [[HU-022-consultar-agenda-profesional]], [[HU-023-cerrar-atencion]].
5. **Incremento 5 — Operación y experiencia integrada:** [[HU-024-consultar-bandeja-administrativa]], [[HU-025-consultar-auditoria-de-estados]], [[HU-027-integrar-pantallas-por-rol]].
6. **Incremento 6 — Automatización posterior:** [[HU-028-enviar-recordatorios]], [[HU-029-notificar-cambios-de-estado]], [[HU-030-generar-resumen-operativo]].

## Decisiones y preguntas abiertas que bloquean implementación selectiva

- El mecanismo exacto de concurrencia/retención de slots, su expiración y el tratamiento de reintentos deben aprobarse antes de [[HU-015-reservar-cita-general]] y [[HU-016-solicitar-cita-especializada]].
- Zona horaria operativa, duración de access/refresh y reset tokens, formato de errores y estrategia de refresh no están definidos por las fuentes.
- El contrato REST se diseña durante el proyecto; [[HU-026-documentar-contrato-rest-base]] debe aprobarse antes de consumidores web estables.
- El framework, rutas y estética de la web dependen del diseño aprobado de Stitch/Google AI Studio; [[HU-027-integrar-pantallas-por-rol]] no autoriza inventarlos.
- Los mecanismos, destinatarios y credenciales de n8n/Gmail se configuran en S5/S6; las HU de EP-010 no cambian el núcleo funcional.
