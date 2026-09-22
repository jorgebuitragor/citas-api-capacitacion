# Decisiones

## DEC-001 — Esquema de referencia como autoridad

- **Estado:** aprobada.
- **Fecha:** 2026-09-22.
- **Decisión:** el esquema inicial es `database/reference/db.sql`; MySQL lo ejecuta al crear el volumen y el backend se adapta a ese contrato de persistencia.
- **Consecuencia:** Flyway aplica baseline `1` al esquema existente y las migraciones evolutivas empiezan en `V2__...`. La API mantiene IDs serializados como texto para conservar el contrato REST de autenticación actual.
