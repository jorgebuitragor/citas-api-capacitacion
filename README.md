# citas-api

Repositorio backend del proyecto. **No contiene implementación de negocio inicial**.

## Debe ser construido por el estudiante
- Java 21 + Spring Boot 3.5.x + Maven.
- Arquitectura hexagonal.
- MySQL + Flyway.
- Spring Security + JWT access/refresh.
- REST.
- Pruebas.

## Documentación compartida
- `docs/wiki/scrum/`: épicas/HU generadas con la Skill Scrum.
- `docs/wiki/llm-wiki/`: única LLM Wiki global del workspace.
- `automations/n8n/`: JSON exportados en S5/S6.

Lee el PRD en la carpeta raíz antes de inicializar Spring Boot.

## Esquema inicial

`database/reference/db.sql` es la autoridad del esquema inicial y Docker lo
carga al crear el volumen MySQL. Flyway registra ese estado como baseline `4`;
las migraciones evolutivas posteriores deben iniciar en `V5__...` y no modificar el SQL de
referencia. Las migraciones V1–V4 existen para evolucionar volúmenes creados por la línea base anterior.
