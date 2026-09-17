---
id: EP-005
tipo: epica
titulo: "Descubrimiento y solicitud de citas"
estado: Pendiente de aprobación
historias: ["[[HU-014-consultar-disponibilidad-para-reserva]]", "[[HU-015-reservar-cita-general]]", "[[HU-016-solicitar-cita-especializada]]", "[[HU-017-decidir-cita-especializada]]"]
dependencias: ["[[EP-004-disponibilidad-del-profesional]]"]
---
# EP-005 — Descubrimiento y solicitud de citas
## Objetivo
Permitir encontrar franjas reservables y crear citas generales o solicitudes especializadas sin doble reserva.
## Valor esperado
Camino principal de agendamiento con decisiones administrativas cuando corresponde.
## Actores
- USER; ADMIN.
## Alcance
- Filtros, duración, retención, aprobación automática general y decisión de especializada.
## Fuera de alcance
- Cancelación, reprogramación, atención y notificaciones.
## Reglas de negocio
- Slots exclusivos/consecutivos; general `APPROVED`; especializada `REQUESTED`; rechazo exige motivo y libera reserva.
## Dependencias
- [[HU-008-gestionar-especialidades]], [[HU-010-asignar-especialidades-al-profesional]], [[HU-012-gestionar-bloques-de-disponibilidad]].
## Historias de usuario
- [[HU-014-consultar-disponibilidad-para-reserva]]
- [[HU-015-reservar-cita-general]]
- [[HU-016-solicitar-cita-especializada]]
- [[HU-017-decidir-cita-especializada]]
## Criterio de completitud de la épica
- [ ] Todas las HU están `Completada`; RN-01 a RN-09 aplicables cuentan con evidencia.
## Riesgos e incógnitas
- Concurrencia, expiración de retención y respuesta ante reintentos son decisiones obligatorias antes de implementación.
