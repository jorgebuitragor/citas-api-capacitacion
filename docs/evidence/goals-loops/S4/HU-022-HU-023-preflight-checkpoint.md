# S4 — Checkpoint de preflight HU-022 / HU-023 (agenda profesional y cierre de atención)

- **Fecha:** 2026-09-29.
- **Resultado:** **PAUSADO / ESCALADO** — no se inició implementación porque no se cumplen las condiciones de entrada declaradas en la meta.
- **Repositorios revisados:** `citas-api` y `citas-web`, ambos en `develop`.
- **Seguridad de lectura:** no se abrieron `.env` ni credenciales.
- **Intentos de implementación consumidos:** 0 de 3 por criterio (no se inició iteración implementación/verificación).

## Fuentes leídas

- Raíz: `README.md`, `PRD.md` (RF-16, RF-17 y RN-11), `RESTRICCIONES_TECNICAS.md`, `database/REQUISITOS_NORMALIZACION_3FN.md`, `AGENTS.md`.
- Repositorios: `citas-api/README.md`, `citas-api/AGENTS.md` (contenido desactualizado respecto al estado real del repo, verificado directamente contra el árbol de archivos), `citas-web/README.md`, `citas-web/AGENTS.md`.
- Especificación: `EP-007-operacion-clinica-del-profesional`, HU-015, HU-017, HU-022, HU-023 y `docs/wiki/scrum/README.md`.
- Contrato: `docs/contracts/appointments.md` (S3 y sección aprobada HU-018/HU-019).
- LLM Wiki: `wiki/index.md`, `decisiones.md` (DEC-001, DEC-002, DEC-003), `riesgos-y-preguntas-abiertas.md`, `trazabilidad-hu.md`, `log.md`.
- Evidencia previa: `docs/evidence/S3-booking-red-green.md`, `docs/evidence/goals-loops/S4/HU-018-HU-019-preflight-checkpoint.md`, `docs/evidence/goals-loops/S4/HU-020-HU-021-preflight-checkpoint.md`.
- Diseño disponible: `citas-web/docs/design/HU-018-HU-019-mis-citas-proposal.md` (no cubre agenda del profesional ni cierre de atención).

## Verificación de condiciones de entrada

| Condición exigida por la meta | Evidencia observada | Estado |
|---|---|---|
| HU-022 aprobada | `estado: Pendiente de aprobación`; CA-01, CA-02 y DoD en tabla de evidencia marcados `Pendiente`, sin enlaces. | **NO satisfecha — bloquea inicio** |
| HU-023 aprobada | `estado: Pendiente de aprobación`; CA-01, CA-02 y DoD `Pendiente`. Nota propia de la HU: «"Pasada/aplicable" es una pregunta abierta explícita». | **NO satisfecha — bloquea inicio** |
| Dependencias de reserva completas (HU-015, HU-017) | Ambas `estado: Aprobada` en su frontmatter y `trazabilidad-hu.md` las registra `HECHO` con evidencia: `BookingIntegrationTest.rejectsSecondReservationWithoutCreatingAnotherAppointment`, `BookingIntegrationTest.adminApprovesOrRejectsRequestedAppointmentsAndKeepsAudit`, contrato S3 y cierre el 2026-09-29. | **Satisfecha** |
| Decisión aprobada que define "pasada/aplicable" | `decisiones.md` solo contiene DEC-001 (esquema de referencia), DEC-002 (interfaz gráfica de autenticación) y DEC-003 (slots/zona horaria S3). Ninguna define elegibilidad temporal ni operativa para cerrar una cita. HU-023 lo marca como pregunta abierta explícita y `EP-007` lo repite en "Riesgos e incógnitas": «Definición operativa de "aplicable" para cerrar una cita requiere aprobación». `riesgos-y-preguntas-abiertas.md` estaba vacío; se añadió la pregunta en este checkpoint (ver sección "Actualización de wiki"). | **NO satisfecha — bloquea inicio** |

## Bloqueos adicionales detectados en el preflight

| Requisito | Evidencia observada | Estado |
|---|---|---|
| Contrato REST de agenda profesional | `docs/contracts/appointments.md` no define ninguna ruta de consulta de agenda por profesional (día/semana/sede) ni proyección de datos con minimización explícita. | Falta |
| Contrato REST de cierre de atención | `docs/contracts/appointments.md` no define ninguna operación de transición a `COMPLETED`/`NO_SHOW`, ni sus códigos de error. | Falta |
| Diseño aprobado de pantallas | Las pantallas obligatorias «dashboard PROFESSIONAL» y «agenda del profesional» (PRD §6) no tienen diseño aprobado. `HU-018-HU-019-mis-citas-proposal.md` cubre solo la vista de citas del USER; no existe propuesta ni aprobación para la vista del PROFESSIONAL. | Falta |
| Definición de "pasada" vs. hora del servidor | El contrato HU-018/HU-019 ya usa `America/Bogota` como zona operativa para "futuro" en cancelación (DEC-003). No hay decisión que extienda ese criterio, u otro distinto, a la elegibilidad de cierre de atención (p. ej., si "pasada" es `endAt < now` o `startAt < now`, y si existe tolerancia/ventana). | Falta |

## Reglas del PRD que la implementación deberá preservar (sin inventar valores)

- RF-16: PROFESSIONAL consulta únicamente sus citas `APPROVED`, filtrables por día/semana y sede; no puede ver datos de usuarios fuera de sus propias citas.
- RF-17: PROFESSIONAL marca una cita propia pasada/aplicable como `COMPLETED` o `NO_SHOW`; debe registrarse historial.
- RN-11: las transiciones de estado deben ser explícitas y verificables.
- RF-19 (transversal): todo cambio de estado audita cita, estado nuevo, actor, fuente y fecha/hora.
- Fuera de alcance explícito de la meta: datos clínicos, diagnósticos, tratamientos o cualquier criterio temporal inventado para "pasada/aplicable".

## Actualización de wiki (documentación, no implementación)

Como mantenedor de la LLM Wiki, se registró la pregunta abierta detectada — ya presente en HU-023 y EP-007, pero ausente de `riesgos-y-preguntas-abiertas.md` — y se añadió una entrada en `log.md`. No se propuso ni infirió ningún valor para "pasada/aplicable"; solo se documentó la ausencia de decisión.

## Decisiones solicitadas para reanudar

1. **Aprobar HU-022 y HU-023** con sus criterios y DoD.
2. **Definir y aprobar el criterio de "pasada/aplicable"** para RF-17 (p. ej., qué instante de la cita se compara contra la hora del servidor en `America/Bogota`, si existe tolerancia, y si aplica igual a citas de 30 y 60 minutos). No se propone valor por inferencia.
3. **Aprobar/documentar el contrato REST** de: consulta de agenda propia del profesional (filtros día/semana/sede, proyección minimizada) y cierre de atención (transición a `COMPLETED`/`NO_SHOW`, autorización, códigos de error).
4. **Aportar/aprobar el diseño visual** de «dashboard PROFESSIONAL» y «agenda del profesional» (PRD §6), con estados `loading`, `empty`, `error`, `success` y `disabled`.

## Alcance preparado para el siguiente intento

Una vez satisfechas las condiciones, el cambio será cross-repo y quedará limitado a HU-022/HU-023:

- **`citas-api`:** puerto y caso de uso de consulta de agenda propia (ownership por profesional autenticado, filtros día/semana/sede, solo `APPROVED`, proyección mínima); puerto y caso de uso de cierre de atención (elegibilidad según la decisión aprobada de "pasada/aplicable", transición a `COMPLETED`/`NO_SHOW`, evento en `appointment_status_history` con actor, fuente y fecha/hora); extensión del contrato REST; autorización por rol y ownership; pruebas de integración para filtros, agenda ajena denegada, cierre válido, cierre de cita futura/ajena/no elegible sin cambios y auditoría.
- **`citas-web`:** vista de agenda del profesional con filtros día/semana/sede ajustada al diseño aprobado; acción de cierre con confirmación y estados de UI; pruebas Vitest, `typecheck` y `build`.
- **Verificación prevista:** `mvn test` en el contenedor API, `npm run typecheck && npm run test && npm run build` en web, y revisión productor-consumidor del contrato.
- **No se incluirá:** datos clínicos/diagnósticos, cambios a citas ajenas o futuras, ni ningún valor de "pasada/aplicable" no aprobado.

## Resultado

- No se modificó código, esquema, contrato, historias (`docs/wiki/scrum/`) ni diseño.
- Se actualizaron únicamente páginas de la LLM Wiki (`riesgos-y-preguntas-abiertas.md`, `log.md`), que son responsabilidad del orquestador, para registrar la pregunta abierta ya existente en HU-023/EP-007.
- Estado final de este intento: **PAUSADO POR PRECONDICIONES Y DECISIÓN ABIERTA**, escalado al usuario.
