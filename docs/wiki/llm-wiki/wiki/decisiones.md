# Decisiones

## DEC-001 — Esquema de referencia como autoridad

- **Estado:** aprobada.
- **Fecha:** 2026-09-22.
- **Decisión:** el esquema inicial es `database/reference/db.sql`; MySQL lo ejecuta al crear el volumen y el backend se adapta a ese contrato de persistencia.
- **Consecuencia:** Flyway aplica baseline `1` al esquema existente y las migraciones evolutivas empiezan en `V2__...`. La API mantiene IDs serializados como texto para conservar el contrato REST de autenticación actual.

## DEC-002 — Aprobación de la interfaz gráfica de autenticación

- **Estado:** aprobada.
- **Fecha:** 2026-09-22.
- **Decisión:** el usuario aprueba explícitamente la interfaz gráfica actual de `citas-web` para registro/login, tal como está implementada en `src/App.tsx`/`src/styles.css` y documentada en `citas-web/skills/web-design-governance/skills/web-design-governance/references/current-web-baseline.md`.
- **Consecuencia:** cierra la pregunta abierta de fuente visual pendiente en HU-002 y HU-003; esa línea base queda como referencia de diseño aprobada para futuras pantallas hasta que se reemplace por una fuente Stitch/AI Studio formal.
