---
id: EP-003
tipo: epica
titulo: "Administración de profesionales"
estado: Pendiente de aprobación
historias: ["[[HU-009-crear-y-administrar-profesional]]", "[[HU-010-asignar-especialidades-al-profesional]]", "[[HU-011-asignar-sedes-al-profesional]]"]
dependencias: ["[[EP-001-acceso-perfil-y-catalogos-fijos]]", "[[EP-002-catalogos-configurables]]"]
---
# EP-003 — Administración de profesionales
## Objetivo
Configurar profesionales ficticios aptos para publicar agenda y atender.
## Valor esperado
Oferta de atención válida por especialidad y sede.
## Actores
- ADMIN; PROFESSIONAL.
## Alcance
- Alta/activación, código y matrícula ficticia, relaciones N:M con especialidades y sedes, especialidad primaria.
## Fuera de alcance
- Bloques de agenda y reserva.
## Reglas de negocio
- Solo ADMIN crea profesionales; cada profesional puede tener varias especialidades/sedes y una primaria.
## Dependencias
- [[HU-006-gestionar-eps]] no es necesaria; sí [[HU-008-gestionar-especialidades]] y [[HU-001-consultar-catalogos-fijos]].
## Historias de usuario
- [[HU-009-crear-y-administrar-profesional]]
- [[HU-010-asignar-especialidades-al-profesional]]
- [[HU-011-asignar-sedes-al-profesional]]
## Criterio de completitud de la épica
- [ ] Todas las HU están `Completada`; relaciones y restricciones son verificables.
## Riesgos e incógnitas
- Regla para retirar una asignación ya usada por agenda/citas requiere decisión explícita.
