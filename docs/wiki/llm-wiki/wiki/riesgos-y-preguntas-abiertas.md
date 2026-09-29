# Riesgos y preguntas abiertas

Esta página se poblará con riesgos verificados y preguntas abiertas. No completar asuntos ambiguos por inferencia.

## PREGUNTA ABIERTA — Criterio de "pasada/aplicable" para cerrar atención (HU-023 / RF-17)

- **Fuente:** RF-17 del PRD dice que PROFESSIONAL puede cerrar una cita "pasada/aplicable"; HU-023 registra explícitamente en sus notas que es una pregunta abierta; EP-007 la repite en "Riesgos e incógnitas".
- **Estado:** sin decisión aprobada. `decisiones.md` no contiene ninguna entrada (DEC-001..DEC-003) que fije este criterio.
- **Lo que falta definir:** qué instante de la cita se compara contra la hora actual de `America/Bogota` (p. ej. `startAt` o `endAt`), si existe alguna tolerancia/ventana, y si el criterio es igual para citas de 30 y 60 minutos.
- **Bloquea:** inicio de implementación de HU-023 (y, por extensión, HU-022 al ser dependencia directa) según la condición de entrada declarada en el goal S4 del 2026-09-29. Ver `docs/evidence/goals-loops/S4/HU-022-HU-023-preflight-checkpoint.md`.
- **RESUELTA — 2026-09-29:** el usuario aprobó el criterio en el chat del goal S4. Ver DEC-006 en `decisiones.md`: "pasada/aplicable" = `endAt <= ahora` en `America/Bogota`, sin tolerancia, igual para 30 y 60 minutos.
