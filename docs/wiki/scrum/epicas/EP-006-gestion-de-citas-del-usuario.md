---
id: EP-006
tipo: epica
titulo: "Gestión de citas del usuario"
estado: Pendiente de aprobación
historias: ["[[HU-018-consultar-mis-citas]]", "[[HU-019-cancelar-cita]]", "[[HU-020-solicitar-reprogramacion]]", "[[HU-021-decidir-reprogramacion]]"]
dependencias: ["[[EP-005-descubrimiento-y-solicitud-de-citas]]"]
---
# EP-006 — Gestión de citas del usuario
## Objetivo
Permitir al USER revisar, cancelar y reprogramar sus citas futuras preservando la cita original hasta la decisión.
## Valor esperado
Autogestión segura y trazable del ciclo de vida.
## Actores
- USER; ADMIN.
## Alcance
- Listado/detalle, cancelación, solicitud y decisión de reprogramación.
## Fuera de alcance
- Cambio de profesional dentro de una reprogramación.
## Reglas de negocio
- Solo cita aprobada/futura se reprograma; la nueva franja queda `PENDING`; la original no se destruye antes de aprobación.
## Dependencias
- [[HU-015-reservar-cita-general]], [[HU-016-solicitar-cita-especializada]], [[HU-017-decidir-cita-especializada]].
## Historias de usuario
- [[HU-018-consultar-mis-citas]]
- [[HU-019-cancelar-cita]]
- [[HU-020-solicitar-reprogramacion]]
- [[HU-021-decidir-reprogramacion]]
## Criterio de completitud de la épica
- [ ] Todas las HU están `Completada`; transiciones y liberación de slots tienen evidencia.
## Riesgos e incógnitas
- Estado exacto de reprogramación y requisito de motivo al rechazo requieren definición contractual.
