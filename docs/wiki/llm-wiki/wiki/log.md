# Registro de la LLM Wiki

## [2026-09-17] scaffold | Inicialización

- Se creó el esquema y el índice inicial de la wiki global.
- No se ingirieron fuentes todavía.
- No se modificaron código, contratos, secretos ni la carpeta `scrum/`.

## [2026-09-22] decisión | Esquema de referencia como autoridad

- DEC-001 aprobada por el usuario: `database/reference/db.sql` se adopta como autoridad de persistencia.
- El backend se adapta a claves numéricas, `roles.code` y `refresh_tokens`; Flyway usa baseline `1` y no conserva el esquema paralelo de autenticación.
- Se conserva el contrato REST de IDs como texto y se actualizan las pruebas para usar MySQL inicializado desde el SQL de referencia.

## [2026-09-22] learn | Cierre técnico parcial de S2

- HECHO: se añadieron migraciones Flyway para alinear el esquema de autenticación activo con entidades numéricas y se validaron las pruebas de integración en el contenedor.
- HECHO: el cliente React compila y su URL de API usa la variable declarada en su `.env.example`.
- HECHO: una investigación acotada delegada revisó el consumo de sesión del cliente; confirma que refresh usa cookies HttpOnly/CSRF y deja como decisión pendiente el almacenamiento del access token.
- PREGUNTA ABIERTA: falta evidencia de ejecución integrada frontend/backend y aprobación explícita de la fuente visual.
- RIESGO: la transición conserva el esquema UUID previo como legado; una migración futura con datos requiere estrategia de traslado aprobada.

## [2026-09-22] decisión | Aprobación de interfaz gráfica de autenticación

- DEC-002 aprobada por el usuario: la interfaz gráfica actual de `citas-web` (pantallas de registro/login en `src/App.tsx`, línea base en `current-web-baseline.md`) queda aprobada como fuente visual.
- Cierra la pregunta abierta de HU-002/HU-003 sobre aprobación explícita de la fuente visual.
- Queda pendiente la demostración end-to-end integrada frontend/backend, que es un asunto distinto de la aprobación visual.
