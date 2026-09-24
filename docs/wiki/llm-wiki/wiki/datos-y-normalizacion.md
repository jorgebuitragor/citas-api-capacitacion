# Datos y normalización

Estado: esquema de referencia adoptado para implementación; pendiente de ampliar la evidencia de pruebas por módulo.

## HECHO

- `database/reference/db.sql` crea el esquema `citas_fcv_training`, normalizado hasta 3FN, con catálogos y datos sintéticos de laboratorio. Fuente: `../../../../../database/reference/db.sql`.

## DECISIÓN

- Aprobada el 2026-09-22: `database/reference/db.sql` es la autoridad de esquema. El backend se ajusta a sus tablas, claves numéricas, `roles.code` y `refresh_tokens`; no mantiene una migración paralela de autenticación.
- Flyway registra la línea base `4` sobre el esquema inicializado por Docker, que ya contiene autenticación y agenda. Las migraciones futuras deben comenzar en `V5__...` y evolucionar este esquema sin modificar `db.sql`; V1–V4 se conservan para volúmenes legados.
