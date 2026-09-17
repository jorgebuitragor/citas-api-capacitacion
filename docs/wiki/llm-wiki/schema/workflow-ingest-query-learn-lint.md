# Workflow operativo

## INGEST

1. Confirmar que la fuente está aprobada y no contiene secretos ni PII innecesaria.
2. Crear un snapshot nuevo en `raw/` y registrarlo en `raw/manifest.md`.
3. Integrar solo conocimiento durable en las páginas afectadas.
4. Actualizar `wiki/index.md` y añadir una entrada append-only a `wiki/log.md`.

## QUERY

Leer primero el índice, después las páginas relevantes y finalmente verificar contra especificaciones y código. Separar evidencia de inferencia en la respuesta.

## LEARN

Persistir solo hechos verificados, decisiones aprobadas, preferencias explícitas o preguntas abiertas todavía relevantes. No guardar transcripciones.

## LINT

Revisar contradicciones, afirmaciones obsoletas, duplicados, páginas huérfanas, enlaces rotos, decisiones no aprobadas y contenido sensible.
