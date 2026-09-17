---
id: EP-010
tipo: epica
titulo: "Automatizaciones posteriores"
estado: Pendiente de aprobación
historias: ["[[HU-028-enviar-recordatorios]]", "[[HU-029-notificar-cambios-de-estado]]", "[[HU-030-generar-resumen-operativo]]"]
dependencias: ["[[EP-008-operacion-administrativa-y-trazabilidad]]"]
---
# EP-010 — Automatizaciones posteriores
## Objetivo
Incorporar automatizaciones n8n sin cambiar el núcleo funcional de citas.
## Valor esperado
Comunicación y visibilidad operativa sobre información sintética.
## Actores
- USER; ADMIN; operación del laboratorio.
## Alcance
- Recordatorios, notificación de cambio y resumen diario mediante n8n/Gmail según fuentes.
## Fuera de alcance
- SMS, WhatsApp, SMTP obligatorio, datos reales y modificaciones del núcleo funcional.
## Reglas de negocio
- Workflows exportados/versionados únicamente en `citas-api/automations/n8n/`; credenciales no se versionan.
## Dependencias
- [[HU-018-consultar-mis-citas]], [[HU-025-consultar-auditoria-de-estados]].
## Historias de usuario
- [[HU-028-enviar-recordatorios]]
- [[HU-029-notificar-cambios-de-estado]]
- [[HU-030-generar-resumen-operativo]]
## Criterio de completitud de la épica
- [ ] Todas las HU están `Completada`, con JSON exportado y sin credenciales/datos reales.
## Riesgos e incógnitas
- Configuración de instancia, destinatarios, calendario y acceso Gmail pertenecen al entorno del trainer/estudiante.
