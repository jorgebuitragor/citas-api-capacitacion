---
id: EP-009
tipo: epica
titulo: "Contrato REST y experiencia web"
estado: Pendiente de aprobación
historias: ["[[HU-026-documentar-contrato-rest-base]]", "[[HU-027-integrar-pantallas-por-rol]]"]
dependencias: ["[[EP-001-acceso-perfil-y-catalogos-fijos]]"]
---
# EP-009 — Contrato REST y experiencia web
## Objetivo
Definir el límite REST y disponer de flujos web por rol fieles al diseño visual aprobado.
## Valor esperado
Integración verificable entre repositorios sin BFF.
## Actores
- USER; PROFESSIONAL; ADMIN.
## Alcance
- Especificación del contrato, autorización/errores y consumo directo desde pantallas obligatorias.
## Fuera de alcance
- Elegir framework o estética antes de Stitch/AI Studio aprobado.
## Reglas de negocio
- URL configurable por environment; backend es autoridad; CORS explícito; no se exponen tokens/secretos.
## Dependencias
- [[HU-003-iniciar-y-cerrar-sesion]]; para cada flujo, la HU funcional correspondiente.
## Historias de usuario
- [[HU-026-documentar-contrato-rest-base]]
- [[HU-027-integrar-pantallas-por-rol]]
## Criterio de completitud de la épica
- [ ] Contratos clave validados por productor y consumidor; pantallas requeridas cumplen estado y accesibilidad aplicable.
## Riesgos e incógnitas
- Framework, diseño, forma de tokens y semántica de errores aún no aprobados.
