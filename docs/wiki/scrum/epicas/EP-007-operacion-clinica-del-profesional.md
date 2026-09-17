---
id: EP-007
tipo: epica
titulo: "Operación clínica del profesional"
estado: Pendiente de aprobación
historias: ["[[HU-022-consultar-agenda-profesional]]", "[[HU-023-cerrar-atencion]]"]
dependencias: ["[[EP-006-gestion-de-citas-del-usuario]]"]
---
# EP-007 — Operación clínica del profesional
## Objetivo
Dar al profesional una agenda acotada a sus citas y permitir cerrar atención aplicable.
## Valor esperado
Seguimiento operativo sin exponer información ajena.
## Actores
- PROFESSIONAL.
## Alcance
- Vista día/semana/sede de aprobadas propias; transición a `COMPLETED` o `NO_SHOW`.
## Fuera de alcance
- Historia clínica, diagnósticos o datos clínicos.
## Reglas de negocio
- Ownership estricto; cierre solo para cita pasada/aplicable; historial obligatorio.
## Dependencias
- [[HU-018-consultar-mis-citas]], [[HU-025-consultar-auditoria-de-estados]].
## Historias de usuario
- [[HU-022-consultar-agenda-profesional]]
- [[HU-023-cerrar-atencion]]
## Criterio de completitud de la épica
- [ ] Todas las HU están `Completada` y no revelan citas fuera del ownership.
## Riesgos e incógnitas
- Definición operativa de “aplicable” para cerrar una cita requiere aprobación.
