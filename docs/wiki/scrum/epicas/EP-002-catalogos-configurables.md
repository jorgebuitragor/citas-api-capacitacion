---
id: EP-002
tipo: epica
titulo: "Catálogos configurables"
estado: Pendiente de aprobación
historias: ["[[HU-006-gestionar-eps]]", "[[HU-007-gestionar-planes-eps]]", "[[HU-008-gestionar-especialidades]]"]
dependencias: ["[[EP-001-acceso-perfil-y-catalogos-fijos]]"]
---
# EP-002 — Catálogos configurables
## Objetivo
Permitir al ADMIN mantener EPS, planes y especialidades activos sin romper referencias históricas.
## Valor esperado
Datos consistentes que habilitan afiliación, oferta profesional y reserva.
## Actores
- ADMIN; USER y PROFESSIONAL como consumidores.
## Alcance
- CRUD autorizado y activación/desactivación de EPS, planes y especialidades, incluida duración 30/60 min.
## Fuera de alcance
- Catálogos fijos y borrado físico de catálogos referenciados.
## Reglas de negocio
- Un catálogo referenciado no se elimina físicamente; especialidad define 30 o 60 min.
## Dependencias
- [[HU-001-consultar-catalogos-fijos]], [[HU-003-iniciar-y-cerrar-sesion]].
## Historias de usuario
- [[HU-006-gestionar-eps]]
- [[HU-007-gestionar-planes-eps]]
- [[HU-008-gestionar-especialidades]]
## Criterio de completitud de la épica
- [ ] Todas las HU están `Completada` y los catálogos mantienen integridad 3FN.
## Riesgos e incógnitas
- La semántica exacta de unicidad/nombre de cada catálogo requiere contrato y modelo propio.
