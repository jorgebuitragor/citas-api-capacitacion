---
id: EP-008
tipo: epica
titulo: "Operación administrativa y trazabilidad"
estado: Pendiente de aprobación
historias: ["[[HU-024-consultar-bandeja-administrativa]]", "[[HU-025-consultar-auditoria-de-estados]]"]
dependencias: ["[[EP-005-descubrimiento-y-solicitud-de-citas]]"]
---
# EP-008 — Operación administrativa y trazabilidad
## Objetivo
Facilitar al ADMIN decidir solicitudes y conservar un historial inmutable de estado.
## Valor esperado
Operación auditable y priorizable.
## Actores
- ADMIN.
## Alcance
- Bandeja de solicitudes/reprogramaciones y consulta de auditoría pertinente.
## Fuera de alcance
- Edición CRUD de eventos de auditoría.
## Reglas de negocio
- Todo cambio registra cita, estado nuevo, actor cuando existe, fuente, fecha/hora y motivo opcional.
## Dependencias
- [[HU-017-decidir-cita-especializada]], [[HU-021-decidir-reprogramacion]].
## Historias de usuario
- [[HU-024-consultar-bandeja-administrativa]]
- [[HU-025-consultar-auditoria-de-estados]]
## Criterio de completitud de la épica
- [ ] Todas las HU están `Completada`, con datos de auditoría no editables como CRUD.
## Riesgos e incógnitas
- Alcance de lectura de auditoría por rol y exposición de actor requieren contrato.
