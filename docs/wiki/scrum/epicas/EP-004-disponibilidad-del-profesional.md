---
id: EP-004
tipo: epica
titulo: "Disponibilidad del profesional"
estado: Pendiente de aprobación
historias: ["[[HU-012-gestionar-bloques-de-disponibilidad]]", "[[HU-013-consultar-calendario-de-disponibilidad]]"]
dependencias: ["[[EP-003-administracion-de-profesionales]]"]
---
# EP-004 — Disponibilidad del profesional
## Objetivo
Permitir publicar y consultar bloques futuros válidos de atención.
## Valor esperado
Fuente confiable de slots de 30 minutos para disponibilidad.
## Actores
- PROFESSIONAL.
## Alcance
- Crear, editar/eliminar bloques futuros sin citas comprometidas y consultar calendario.
## Fuera de alcance
- Reserva o reasignación automática de citas.
## Reglas de negocio
- Sin pasado ni solapes; sede asignada; cada bloque se discretiza en slots de 30 min.
## Dependencias
- [[HU-009-crear-y-administrar-profesional]], [[HU-011-asignar-sedes-al-profesional]].
## Historias de usuario
- [[HU-012-gestionar-bloques-de-disponibilidad]]
- [[HU-013-consultar-calendario-de-disponibilidad]]
## Criterio de completitud de la épica
- [ ] Todas las HU están `Completada` con pruebas de reglas temporales y solapamiento.
## Riesgos e incógnitas
- Tratamiento preciso de bloque con cita `REQUESTED`/retención debe quedar alineado con reserva.
