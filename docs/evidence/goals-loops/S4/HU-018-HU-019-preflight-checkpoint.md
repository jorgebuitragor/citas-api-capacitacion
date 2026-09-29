# S4 — Checkpoint de preflight HU-018 / HU-019

- **Fecha:** 2026-09-24.
- **Resultado:** PAUSADO — no se inició implementación porque no se cumplen las condiciones de entrada indicadas en la solicitud.
- **Repositorios revisados:** `citas-api` y `citas-web`, ambos en `develop`.
- **Seguridad de lectura:** no se abrieron archivos `.env` ni credenciales.

## Fuentes revisadas

- Raíz: `README.md`, `PRD.md` (RF-13, RF-14, RN-09, RN-11), `RESTRICCIONES_TECNICAS.md`, `database/REQUISITOS_NORMALIZACION_3FN.md`.
- Repositorios: `citas-api/README.md`, `citas-web/README.md`, ambos `AGENTS.md`.
- Especificación: HU-015, HU-016, HU-018, HU-019 y mapa `docs/wiki/scrum/README.md`.
- Contrato y wiki: `citas-api/docs/contracts/appointments.md`, índice LLM Wiki y páginas de contratos, decisiones y trazabilidad.
- Evidencia previa: `citas-api/docs/evidence/S3-booking-red-green.md`.
- Diseño disponible: `citas-web/skills/web-design-governance/skills/web-design-governance/references/current-web-baseline.md` y DEC-002.
- Implementación observada de solo lectura: controladores/servicio/pruebas de booking del API, `citas-web/src/App.tsx` y `citas-web/package.json`.

## Bloqueos de entrada

| Requisito | Evidencia observada | Estado |
|---|---|---|
| HU-018 aprobada | Frontmatter de `HU-018-consultar-mis-citas.md`: `estado: Pendiente de aprobación`; tabla de CA/DoD pendiente. | Bloquea inicio |
| HU-019 aprobada | Frontmatter de `HU-019-cancelar-cita.md`: `estado: Pendiente de aprobación`; tabla de CA/DoD pendiente. | Bloquea inicio |
| HU-015 completada | Frontmatter continúa `Pendiente de aprobación`; la evidencia S3 documenta reserva general y pruebas, pero no cierra formalmente CA/DoD de HU-015. | No acreditada |
| HU-016 completada | Frontmatter continúa `Pendiente de aprobación`; la evidencia S3 no declara cierre de CA/DoD de solicitud especializada. | No acreditada |
| Contrato aprobado para esta entrega | `appointments.md` documenta catálogos, disponibilidad, creación y decisión ADMIN; no define endpoint, filtros, campos ni errores de consulta propia/cancelación. | Falta |
| Diseño aprobado de citas | DEC-002 aprueba únicamente login/registro. La línea base visual dice que no se encontró una fuente de diseño aprobada más reciente y no define pantalla de mis citas. | Falta |

## Decisiones solicitadas para reanudar

1. Aprobar HU-018 y HU-019, incluyendo sus criterios y DoD.
2. Cerrar formalmente HU-015 y HU-016 con evidencia enlazada en sus historias; para HU-016 incluir evidencia de solicitud especializada, slots retenidos y contrato consumido.
3. Aprobar/documentar el contrato REST de consulta propia (filtros/orden, respuesta y motivo de rechazo) y cancelación (transición, resultado y errores para propiedad/estado/fecha). La zona horaria operativa ya consta como `America/Bogota` en DEC-003 y no se propone otra regla.
4. Proveer/aprobar el diseño de la vista de citas y sus acciones; DEC-002 no cubre esta pantalla.

## Alcance preparado para el siguiente intento

Una vez satisfechas las condiciones anteriores, el cambio será cross-repo y se limitará a HU-018/HU-019:

- `citas-api`: caso de uso de listado filtrado por principal autenticado; proyección con sede, profesional, especialidad, inicio/fin/duración, estado y motivo de rechazo; cancelación transaccional de cita propia futura no terminal; liberación de slots e historial de estado; pruebas de propiedad, fecha, terminalidad, liberación e historial; contrato y trazabilidad/evidencia.
- `citas-web`: pantalla ajustada al diseño aprobado; filtros por estado/fecha; estados loading, empty, error y success; detalle requerido; cancelación con prevención de doble envío y actualización visible; pruebas y validación de integración.
- No se incluirán reprogramación, cierre de atención, auditoría general ni ventana adicional de cancelación.

## Resultado y próximo checkpoint

- No se modificó código, contrato, historias ni diseño. Este archivo es el checkpoint solicitado y conserva el resultado del preflight.
- Estado final de este intento: **PAUSADO POR PRECONDICIONES AUSENTES**.
- Próximo checkpoint: repetir preflight documental tras la aprobación de HU-018/HU-019, el cierre evidenciado de HU-015/HU-016 y la disponibilidad del contrato y diseño aprobados; entonces presentar el plan cross-repo previo a editar.

## Revalidación de continuación

- **Fecha:** 2026-09-24.
- Se volvieron a consultar los frontmatter y tablas de evidencia de HU-015, HU-016, HU-018 y HU-019; las cuatro siguen `Pendiente de aprobación` y sin cierre DoD.
- `docs/contracts/appointments.md` sigue sin endpoints/campos/errores para consulta propia y cancelación.
- El inventario de archivos de diseño sigue mostrando solo la línea base de autenticación; no apareció diseño aprobado de la vista de citas.
- No se editó implementación. Los cambios locales preexistentes en `citas-web/src/App.tsx`, `citas-web/src/App.test.tsx` y la migración V5 de `citas-api` se conservaron intactos.
- Se solicitó al usuario ubicar/aportar las aprobaciones y artefactos o autorizar propuestas para su revisión.

## Auditoría de bloqueo

- **Fecha:** 2026-09-24.
- Revalidación final: HU-015, HU-016, HU-018 y HU-019 continúan en `Pendiente de aprobación`; el contrato no tiene operaciones de consulta/cancelación y no hay artefacto de diseño aprobado para Mis citas.
- La búsqueda también encontró materiales de la skill Stitch, que son instrucciones y referencias del proceso, no un diseño aprobado de esta pantalla.
- La continuación no aportó aprobación, evidencia de cierre ni ubicación de contrato/diseño; tampoco autorizó preparar propuestas.
- Se alcanza el umbral de tres turnos consecutivos con la misma precondición ausente. Estado del objetivo: **BLOQUEADO**, a la espera del cambio documental/decisión del usuario.

## Propuesta de diseño solicitada

- **Fecha:** 2026-09-24.
- El usuario pidió crear el diseño de la pantalla con la skill de diseño frontend.
- Se leyó `citas-web/.claude/skills/stitch-design-to-frontend/SKILL.md` y sus referencias de calidad, setup MCP, selección de modo, prompting y handoff; también la skill `web-design-governance` y la línea base visual existente.
- La inspección de herramientas no encontró Stitch MCP. Se siguió la alternativa manual permitida por la skill: brief de diseño y prompt autocontenido listo para pegar en Stitch.
- Artefacto creado: `citas-web/docs/design/HU-018-HU-019-mis-citas-proposal.md`.
- **Estado:** propuesta pendiente de generación/revisión visual en Stitch y aprobación explícita. No se afirmó que Stitch generara una pantalla ni se hizo handoff a Google AI Studio.
- El artefacto define una sola vista, filtros de estado/fecha, los campos RF-13, motivo de rechazo, cancelación futura no terminal, diálogo y estados de UI/responsive/accesibilidad. No especifica contrato API ni añade funcionalidades fuera del alcance.
- La aprobación de HU-018/HU-019 y la evidencia de cierre de HU-015/HU-016 siguen pendientes; por ello no se inicia implementación frontend/backend.

## Intento de generación visual en Stitch

- **Fecha:** 2026-09-24.
- Se abrió `https://stitch.withgoogle.com/` en el navegador de Codex, se eligió el formato Web y se introdujo el prompt de diseño guardado en la propuesta.
- Al solicitar la generación, Stitch redirigió a `accounts.google.com` para iniciar sesión. La generación no se ejecutó y no se introdujeron credenciales.
- La pestaña quedó marcada para handoff; el usuario debe completar el inicio de sesión directamente en el navegador y avisar para reanudar la generación.
- Estado del artefacto: prompt listo; generación de pantalla y revisión visual todavía pendientes.

## Revalidación posterior al cierre de S3

- **Fecha:** 2026-09-29.
- **S3:** HU-015, HU-016 y HU-017 quedaron aprobadas y cerradas con CA/DoD, contrato, pruebas y hooks documentados.
- **HU-018/HU-019:** continúan `Pendiente de aprobación`; no se inicia implementación S4.
- **Contrato:** `docs/contracts/appointments.md` sigue sin definir las operaciones REST de consulta propia y cancelación requeridas por S4.01.
- **Diseño:** `citas-web/docs/design/HU-018-HU-019-mis-citas-proposal.md` continúa como propuesta pendiente de generación/revisión/aprobación visual.
- **Resultado:** S4.01 permanece **BLOQUEADO POR PRECONDICIONES AUSENTES**, ahora con la dependencia S3 satisfecha.

## Revalidación del objetivo — 2026-09-29

- **Alcance revalidado:** se leyeron PRD (RF-13, RF-14, RN-09, RN-11), `RESTRICCIONES_TECNICAS.md`, HU/DoD 015/016/018/019, contrato `citas-api/docs/contracts/appointments.md`, AGENTS de raíz/API/web, índice y páginas pertinentes de LLM Wiki, evidencia S3 y artefactos de diseño disponibles.
- **HU-015/HU-016:** aprobadas y cerradas el 2026-09-29 en sus historias, con CA/DoD marcados cumplidos y referencias a `BookingIntegrationTest`, evidencia S3 y contrato. Precondición satisfecha.
- **HU-018/HU-019:** ambas permanecen `Pendiente de aprobación`; sus CA y DoD también siguen pendientes. Precondición no satisfecha.
- **Contrato REST:** no define consulta de citas propias (ruta, filtros de estado/fecha, proyección de sede/profesional/especialidad/fecha-hora/duración/estado/motivo) ni cancelación (operación, respuesta y errores de ownership/fecha/estado). No es seguro inferirlo.
- **Diseño:** `citas-web/docs/design/HU-018-HU-019-mis-citas-proposal.md` se declara propuesta pendiente de generación/revisión/aprobación. DEC-002 aprueba solo autenticación; `current-web-baseline.md` es una referencia observada, no aprobación visual para esta pantalla.
- **Resultado de este checkpoint:** **PAUSADO / BLOQUEADO POR PRECONDICIONES AUSENTES**. No se editó código, contrato, historias ni diseño. HU-015/HU-016 quedan acreditadas; siguen bloqueando la aprobación formal de HU-018/HU-019 y el contrato/diseño aprobados.
- **Para reanudar:** aprobar HU-018/HU-019 y sus CA/DoD; aprobar/documentar el contrato REST de consulta/cancelación; aportar diseño visual generado/revisado y aprobado para “Mis citas”. Después realizar plan cross-repo previo a editar. No añadir ventanas de cancelación ni alcance fuera de estas HU.
- **Intentos:** 0 de 3 para criterios de implementación (no inició iteración de implementación/verificación).

## Continuación tras instrucción del usuario — 2026-09-29

- **HU-018/HU-019:** aprobadas explícitamente por el usuario. Se actualizó `estado: Aprobada` y se registró la fuente de aprobación en ambas historias; CA y DoD siguen intactos. El índice Scrum incluye ahora las dos HU en la lista aprobada.
- **Dependencias HU-015/HU-016:** continúan satisfechas con aprobación/cierre y evidencia S3 documentados.
- **Preflight restante:** contrato de consulta/cancelación aún no aprobado ni definido en `docs/contracts/appointments.md`; la vista disponible sigue siendo una propuesta sin generación/revisión visual ni aprobación. Esas condiciones impiden iniciar implementación cross-repo bajo el criterio original de la meta.
- **Resultado:** checkpoint actualizado; no se inició código ni se modificaron reglas/contratos/diseño. Máximo de intentos por criterio: 0/3.

## Definición del contrato HU-018/HU-019 — 2026-09-29

- A solicitud del usuario se redactó una **propuesta** en `docs/contracts/appointments.md`: listado propio con filtros opcionales status/date, campos para la tarjeta, cancelación transaccional, ownership desde JWT, liberación de slots, auditoría y errores.
- Los códigos de estado, zona horaria, historial y slots se contrastaron con `V3__add_scheduling_schema.sql`, `AppointmentStatus`, el controlador/servicio actuales y el manejador HTTP. La propuesta señala ajustes de autorización y mapeo HTTP necesarios para implementación.
- **Pendiente:** aprobación de esta propuesta por el usuario. No se modificó código ni se inició implementación.
- **Diseño:** la propuesta escrita de pantalla sigue sin generación/revisión visual y aprobación. Por tanto, el plan cross-repo continúa bloqueado hasta cerrar ambos artefactos.

## Aprobación del contrato — 2026-09-29

- El usuario aprobó explícitamente la propuesta HU-018/HU-019 registrada en `docs/contracts/appointments.md`.
- El contrato queda aprobado como base para la implementación; sus rutas, filtros, ownership JWT, proyección, transición y errores quedan fijados según esa sección.
- No se inició implementación porque sigue faltando un diseño visual generado y aprobado de “Mis citas”, condición de entrada original para el cambio cross-repo.
- **Preflight restante:** aprobación visual del diseño de HU-018/HU-019. Luego se presentará/seguirá el plan cross-repo y se iniciará el trabajo.

## Diseño recibido y plan cross-repo — 2026-09-29

- El usuario compartió `C:/Users/IA ACADEMY 4/Downloads/medischedule---portal-de-citas-médicas/` como referencia visual y pidió adaptarla. Se considera aprobada para guiar la adaptación visual de HU-018/HU-019.
- Se identificaron vistas/componentes de Mis citas, filtros, tarjeta, detalle, modal de cancelación y estados de carga/error/vacío/éxito. El prototipo también contiene reprogramación, otras secciones y datos ficticios no compatibles; se excluyen del alcance implementado. No se copiarán instrucciones/configuración de Gemini/Express del proyecto fuente.
- **Plan API (`citas-api`):** `BookingPorts.java`, `BookingService.java`, `BookingPersistenceAdapter.java`, `BookingController.java`, `BookingException.java`, `ApiExceptionHandler.java`, `SecurityConfiguration.java`, `AppointmentStatus.java`, `BookingIntegrationTest.java`. Añadir consulta filtrada propiedad del principal JWT; proyección de citas y motivo; elegibilidad; cancelación protegida/transaccional; liberar slots y registrar evento USER; validar errores/roles/propiedad/estado/fecha.
- **Plan web (`citas-web`):** `src/api/booking.ts`, `src/App.tsx`, `src/styles.css`, `src/App.test.tsx` y, si mejora el aislamiento, un componente dedicado en `src/`. Integrar el contrato, mostrar campos/filtros/estados, confirmar y prevenir doble envío; conservar búsqueda de disponibilidad y retirar de la vista las acciones de reprogramación y simulación del prototipo.
- **Validación prevista:** pruebas de integración backend para listado/filtros/ownership/rechazo/cancelación/liberación/historial/estados no elegibles; frontend typecheck, Vitest y build; revisión contrato consumidor-productor.
- Los cambios preexistentes en ambos repos fueron inspeccionados y se preservarán; no se tocarán migraciones V5/V6 ni el trabajo S3 ya preparado.

## Implementación y validación — 2026-09-29

- **HU-018 y HU-019:** implementación cross-repo completada según contrato aprobado y referencia visual `medischedule---portal-de-citas-médicas`.
- **API:** `GET /api/v1/appointments?status=&date=` filtra únicamente por principal USER, con orden estable e información relacionada/rechazo aplicable. `POST /api/v1/appointments/{id}/cancellation` bloquea por ID+propietario; permite solo futuras no terminales, cambia a `CANCELLED`, libera slots e inserta historial USER dentro de la transacción. Citas ajenas devuelven 404; pasada/terminal/cancelada 409 sin mutación.
- **Web:** vista “Mis citas” adaptada al diseño compartido, filtros, campos mínimos, loading/empty/error/success, reintento, confirmación accesible y prevención de doble envío. No se incluyeron reprogramación, cierre de atención ni ventanas adicionales.
- **Persistencia:** no se requiere migración; el esquema V3 ya contiene estados terminales, slots e historial.
- **Validaciones:** `docker compose exec -T citas-api-dev mvn test` — BUILD SUCCESS, 15 tests, 0 fallos/errores (AuthIntegrationTest 5, BookingIntegrationTest 10; Flyway validó 6 migraciones y esquema versión 6 al día). En `citas-web`: `npm run typecheck` — OK; `npm run test` — 2 archivos/8 tests OK; `npm run build` — OK. La primera repetición local de Vitest encontró `spawn EPERM`; se volvió a ejecutar con permisos aprobados y pasó. `git diff --check` termina con código 1 por líneas en blanco adicionales al final de `docs/contracts/appointments.md`, `docs/wiki/scrum/README.md` y ambas historias HU-018/HU-019; no reporta otros problemas de whitespace. Se conservan para no reformatear cambios documentales ajenos al alcance funcional.
- **Trazabilidad:** historias HU-018/HU-019 actualizadas a `Completada`, con CA/DoD enlazados a símbolos y pruebas concretas. HU-015/HU-016/017, S3, V5/V6 y demás cambios preexistentes se preservaron.
- **Resultado final:** objetivo HU-018/HU-019 cumplido. Contrato productor/consumidor validado mediante integración backend, pruebas web, typecheck y build; no queda bloqueo conocido dentro del alcance. No se hizo commit.
