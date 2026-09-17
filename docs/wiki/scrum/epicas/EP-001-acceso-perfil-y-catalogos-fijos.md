---
id: EP-001
tipo: epica
titulo: "Acceso, perfil y catálogos fijos"
estado: Pendiente de aprobación
historias: ["[[HU-001-consultar-catalogos-fijos]]", "[[HU-002-registrar-usuario]]", "[[HU-003-iniciar-y-cerrar-sesion]]", "[[HU-004-recuperar-contrasena]]", "[[HU-005-gestionar-perfil-y-afiliacion]]"]
dependencias: []
---
# EP-001 — Acceso, perfil y catálogos fijos
## Objetivo
Permitir a personas ficticias acceder de forma segura y mantener su identidad y afiliación.
## Valor esperado
Base autenticada y normalizada para reservar y operar citas.
## Actores
- USER; ADMIN para los catálogos; PROFESSIONAL como consumidor del contexto de acceso.
## Alcance
- Registro, JWT access/refresh, logout, recuperación, perfil, afiliación y lectura de catálogos fijos.
## Fuera de alcance
- Gestión de EPS, planes y especialidades configurables: [[EP-002-catalogos-configurables]].
## Reglas de negocio
- Email/documento únicos; password con hash adaptativo; roles y catálogos fijos precargados; no duplicar EPS/régimen/plan en la afiliación.
## Dependencias
- Ninguna funcional previa; depende de decisiones técnicas pendientes de [[HU-026-documentar-contrato-rest-base]].
## Historias de usuario
- [[HU-001-consultar-catalogos-fijos]]
- [[HU-002-registrar-usuario]]
- [[HU-003-iniciar-y-cerrar-sesion]]
- [[HU-004-recuperar-contrasena]]
- [[HU-005-gestionar-perfil-y-afiliacion]]
## Criterio de completitud de la épica
- [ ] Todas las HU obligatorias están `Completada` con evidencia.
- [ ] Seguridad, modelo 3FN y contrato correspondiente son coherentes.
## Riesgos e incógnitas
- Vencimientos de tokens, zona horaria y canal seguro de entrega del reset pendientes de decisión.
