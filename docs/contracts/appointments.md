# Contrato REST — disponibilidad y reservas S3

Zona horaria operativa: `America/Bogota`. Las fechas y horas se intercambian como valores locales ISO-8601 sin offset.

| Método y ruta | Rol | Operación |
|---|---|---|
| `GET /api/v1/catalogs/locations` | USER | Sedes activas |
| `GET /api/v1/catalogs/specialties` | USER | Especialidades activas |
| `GET /api/v1/catalogs/professionals?locationId&specialtyId` | USER | Profesionales habilitados |
| `GET /api/v1/availability?locationId&specialtyId&professionalId&date` | USER | Franjas completas disponibles |
| `POST /api/v1/appointments` | USER | Crea reserva general o solicitud especializada |
| `GET /api/v1/admin/appointments?status=REQUESTED` | ADMIN | Solicitudes especializadas pendientes |
| `POST /api/v1/admin/appointments/{id}/decision` | ADMIN | Aprueba o rechaza una solicitud |

## Crear cita

`POST /api/v1/appointments` recibe `locationId`, `specialtyId`, `professionalId` y `startAt`.
El servidor determina duración y estado: Medicina General crea `APPROVED`; una especialidad con aprobación administrativa crea `REQUESTED`. Los IDs se serializan como cadenas.

Una respuesta exitosa es `201` e incluye `id`, `status`, `startAt`, `endAt` y `durationMinutes`. Una franja ocupada o incompleta devuelve `409 application/problem+json`; datos inválidos devuelven `400`.

## Decisión ADMIN

`POST /api/v1/admin/appointments/{id}/decision` recibe `decision` (`APPROVE` o `REJECT`) y `reason` para `REJECT`.
Solo una solicitud `REQUESTED` es decidible. Aprobar conserva los slots; rechazar con motivo los libera. USER y PROFESSIONAL reciben `403`.

## HU-018 / HU-019 — contrato aprobado e implementado

Estado: **Aprobado por el usuario e implementado el 2026-09-29.** Conserva el formato existente: IDs como cadenas y fecha/hora local ISO-8601 sin offset en `America/Bogota`.

### Consultar citas propias

`GET /api/v1/appointments?status={status}&date={yyyy-MM-dd}` — solo `USER` autenticado.

- `status` es opcional y acepta un código del catálogo de estados: `REQUESTED`, `APPROVED`, `REJECTED`, `CANCELLED`, `COMPLETED`, `NO_SHOW`.
- `date` es opcional y filtra por el día local de inicio de la cita. Si ambos filtros se envían, se aplican conjuntamente (AND). Sin filtros, se listan todas las citas propias.
- El backend obtiene el propietario del principal JWT. No se acepta `patientUserId`, `userId` ni otro selector de propietario desde query/body.
- El resultado se limita siempre al usuario autenticado y se ordena por `startAt` ascendente; los empates, por `id` ascendente. No se agrega paginación.
- Respuesta `200 application/json`:

```json
{
  "items": [
    {
      "id": "84",
      "location": { "id": "1", "name": "Hospital Internacional de Colombia (HIC)" },
      "professional": { "id": "12", "name": "Dra. Ejemplo" },
      "specialty": { "id": "1", "name": "Medicina General" },
      "startAt": "2026-10-01T09:00:00",
      "endAt": "2026-10-01T09:30:00",
      "durationMinutes": 30,
      "status": "APPROVED",
      "rejectionReason": null,
      "cancellationAllowed": true
    }
  ]
}
```

- `rejectionReason` contiene el motivo del evento de rechazo cuando el estado actual es `REJECTED`; en otro caso es `null`. La API obtiene el motivo del historial de transición, no duplica ese dato en la cita.
- `cancellationAllowed` informa si, al momento de construir la respuesta, el estado no es terminal y `startAt` es posterior a la hora actual de `America/Bogota`. Es una ayuda de presentación; el endpoint de cancelación vuelve a validar todo bajo bloqueo transaccional.
- Sin coincidencias: `200` con `items: []`.

### Cancelar cita propia

`POST /api/v1/appointments/{id}/cancellation` — solo `USER` autenticado; no requiere body.

- El propietario se toma exclusivamente del principal JWT.
- Solo se permite cuando `startAt` es estrictamente posterior a la hora actual de `America/Bogota` y el estado actual no es terminal. Según el catálogo actual, los estados no terminales son `REQUESTED` y `APPROVED`; son terminales `REJECTED`, `CANCELLED`, `COMPLETED` y `NO_SHOW`.
- La transición es explícita y atómica: el estado pasa a `CANCELLED`, todos los slots asociados quedan libres y se inserta un evento en `appointment_status_history` con actor USER autenticado, fuente `USER` y fecha/hora del servidor. La operación no reactiva citas ni admite un motivo proporcionado por el cliente.
- Éxito: `200 application/json` con el objeto de cita completo del contrato de listado, actualizado a `CANCELLED` y `cancellationAllowed: false`.
- Repetir la solicitud sobre una cita ya cancelada devuelve `409` sin cambiar cita, slots ni historial.

### Errores de consulta/cancelación

Los errores usan `application/problem+json`, con la forma estándar existente `status`, `title` y `detail`.

| HTTP | Caso |
|---|---|
| `400` | `status` desconocido, `date` mal formado o `{id}` no válido. |
| `401` | No hay autenticación válida. |
| `403` | El principal autenticado no tiene rol `USER`. |
| `404` | La cita no existe o no pertenece al usuario autenticado. Se usa la misma respuesta en ambos casos para no revelar existencia ajena. No produce cambios. |
| `409` | La cita propia ya no es cancelable por ser pasada, terminal, estar cancelada o haber cambiado concurrentemente de estado. No produce cambios. |

### Decisiones de implementación asociadas

- La autorización debe cubrir `/api/v1/appointments/**` para `USER`, no solo el path raíz de creación que hoy aparece en Security.
- La cita, slots e historial se leen/modifican dentro de una transacción con bloqueo de la cita; solo tras validar ownership, estado y futuro se libera la reserva y se registra `CANCELLED`.
- La excepción de estado inválido para cancelación se mapeará a `409` (el manejador actual convierte `INVALID_STATE` a `400`); validaciones de parámetros siguen en `400`.
- La consulta no expone historial general ni datos de otros usuarios.

## HU-020 / HU-021 — contrato de reprogramación (APROBADO)

Estado: **Aprobado por el usuario el 2026-09-29.** Conserva el formato existente: IDs como cadenas y fecha/hora local ISO-8601 sin offset en `America/Bogota`. Reglas de referencia: RF-15 y RN-01, RN-05, RN-09, RN-10, RN-11, más DEC-003 (concurrencia y retención) y DEC-005 (motivo).

| Método y ruta | Rol | Operación |
|---|---|---|
| `POST /api/v1/appointments/{id}/reschedule-requests` | USER | Solicita reprogramar una cita propia `APPROVED` y futura |
| `GET /api/v1/admin/reschedule-requests?status=PENDING` | ADMIN | Bandeja de reprogramaciones pendientes |
| `POST /api/v1/admin/reschedule-requests/{id}/decision` | ADMIN | Aprueba o rechaza una reprogramación `PENDING` |

Además, el objeto de cita del contrato HU-018 se **extiende de forma aditiva** con `rescheduleAllowed` y `rescheduleRequest`. Los consumidores existentes no se rompen porque no se elimina ni renombra ningún campo.

### Estados de la solicitud

Catálogo `reschedule_request_statuses`: `PENDING` (no terminal), `APPROVED`, `REJECTED`, `CANCELLED` (terminales). Esta entrega usa `PENDING`, `APPROVED` y `REJECTED`; `CANCELLED` queda en el catálogo sin transición expuesta porque la cancelación de la solicitud por el usuario no pertenece a HU-020/HU-021.

El estado de la **cita** no cambia mientras la solicitud está `PENDING`: permanece `APPROVED`. No se introduce ningún código nuevo en `appointment_statuses`.

### Solicitar reprogramación

`POST /api/v1/appointments/{id}/reschedule-requests` — solo `USER` autenticado. `{id}` es la cita original.

```json
{ "locationId": "1", "specialtyId": "1", "professionalId": "12", "startAt": "2026-10-02T09:00:00" }
```

- Los cuatro campos son obligatorios. Se pide `professionalId` y `specialtyId` explícitamente —aunque el servidor los conoce por la cita original— para que un intento de cambio de profesional o especialidad sea un error explícito y verificable en lugar de una sustitución silenciosa.
- El propietario se toma exclusivamente del principal JWT; no se acepta ningún selector de propietario.
- Elegibilidad de la cita original: existe, pertenece al usuario autenticado, su estado actual es `APPROVED`, su `startAt` es estrictamente posterior a la hora actual de `America/Bogota` y no tiene ya una solicitud `PENDING`.
- `professionalId` y `specialtyId` deben coincidir con los de la cita original (RF-15). Cualquier diferencia es `400` y no crea nada.
- **Sede (aprobado):** RF-15 fija profesional y especialidad, no sede; el esquema de referencia guarda `requested_location_id` en la solicitud. `locationId` puede ser cualquiera de las sedes habilitadas para ese profesional y especialidad, y la franja se busca en la agenda de esa sede.
- La nueva franja se valida como en la reserva S3: `startAt` futuro y alineado a 30 minutos, franja completa según la duración de la especialidad (uno o dos slots consecutivos, RN-05) y todos sus slots libres (RN-01), con bloqueo pesimista ascendente dentro de una única transacción.
- La nueva franja no puede solaparse con la franja original de la propia cita.
- **Retención (RN-10) — mecanismo aprobado:** los slots de la nueva franja se marcan con `professional_slots.reschedule_request_id = {idSolicitud}` y conservan `appointment_id = NULL`. Los slots de la franja original conservan su `appointment_id` intacto. La cita mantiene `scheduled_start_at`/`scheduled_end_at` originales y la solicitud guarda `previous_start_at`/`previous_end_at` y `requested_start_at`/`requested_end_at`. Así la retención provisional es explícita y distinguible de una reserva firme.
- **Definición de slot libre:** a partir de esta entrega un slot está libre únicamente cuando `appointment_id IS NULL` **y** `reschedule_request_id IS NULL`. La consulta de disponibilidad (`GET /api/v1/availability`) y la creación de citas aplican ambas condiciones, de modo que una franja retenida por una reprogramación pendiente no se ofrece ni se puede reservar (RN-01, HU-014 CA-03).
- Respuesta `201 application/json`:

```json
{
  "id": "5",
  "appointmentId": "84",
  "status": "PENDING",
  "previousStartAt": "2026-10-01T09:00:00",
  "previousEndAt": "2026-10-01T09:30:00",
  "requestedStartAt": "2026-10-02T09:00:00",
  "requestedEndAt": "2026-10-02T09:30:00",
  "durationMinutes": 30,
  "location": { "id": "1", "name": "Hospital Internacional de Colombia (HIC)" },
  "decisionReason": null,
  "decidedAt": null
}
```

- **Auditoría:** se inserta un evento en `appointment_status_history` con el estado resultante de la cita (`APPROVED`, que no cambia), actor USER autenticado, fuente `USER`, fecha/hora del servidor y motivo textual que identifica la transición de solicitud de reprogramación con la franja pedida.

### Extensión del objeto de cita (HU-018)

Cada elemento de `GET /api/v1/appointments` y la respuesta de cancelación añaden:

```json
{
  "rescheduleAllowed": true,
  "rescheduleRequest": {
    "id": "5",
    "status": "PENDING",
    "requestedStartAt": "2026-10-02T09:00:00",
    "requestedEndAt": "2026-10-02T09:30:00",
    "decisionReason": null,
    "decidedAt": null
  }
}
```

- `rescheduleAllowed` es `true` cuando el estado es `APPROVED`, `startAt` es futuro y no existe una solicitud `PENDING`. Es ayuda de presentación; el endpoint revalida todo bajo bloqueo.
- `rescheduleRequest` contiene la solicitud **más reciente** de esa cita, o `null` si nunca hubo ninguna. Cuando su `status` es `REJECTED`, `decisionReason` lleva el motivo del rechazo, que es el dato que el frontend muestra al USER.

### Bandeja ADMIN

`GET /api/v1/admin/reschedule-requests?status=PENDING` — solo `ADMIN`.

- `status` es opcional, admite `PENDING`, `APPROVED` o `REJECTED`, y por omisión es `PENDING`. Un valor desconocido es `400`.
- Orden estable por `requestedStartAt` ascendente y, en empate, `id` ascendente. Sin paginación.
- Respuesta `200 application/json`:

```json
{
  "items": [
    {
      "id": "5",
      "appointmentId": "84",
      "status": "PENDING",
      "patientName": "Paciente Sintético",
      "professionalName": "Dra. Ejemplo",
      "specialtyName": "Medicina General",
      "locationName": "Hospital Internacional de Colombia (HIC)",
      "durationMinutes": 30,
      "previousStartAt": "2026-10-01T09:00:00",
      "previousEndAt": "2026-10-01T09:30:00",
      "requestedStartAt": "2026-10-02T09:00:00",
      "requestedEndAt": "2026-10-02T09:30:00",
      "decisionReason": null,
      "decidedAt": null
    }
  ]
}
```

### Decisión ADMIN

`POST /api/v1/admin/reschedule-requests/{id}/decision` — solo `ADMIN`.

```json
{ "decision": "REJECT", "reason": "El profesional no estará disponible ese día." }
```

- `decision` es obligatorio y admite `APPROVE` o `REJECT`.
- **DEC-005:** `reason` es obligatorio y no vacío para `REJECT`; su ausencia es `400` y no produce ningún cambio. Para `APPROVE` es opcional y, si se envía, se conserva en la auditoría y en `decision_reason`. Longitud máxima 500 caracteres, igual que `appointment_status_history.reason`.
- Solo una solicitud `PENDING` es decidible. Todo se ejecuta en una única transacción con bloqueo de la solicitud y de la cita.
- **Aprobar (RN-09, RN-10):** libera los slots de la franja original (`appointment_id = NULL`), convierte la retención en reserva firme en los slots de la nueva franja (`appointment_id = {cita}`, `reschedule_request_id = NULL`), actualiza `scheduled_start_at`/`scheduled_end_at` de la cita a la franja solicitada, marca la solicitud `APPROVED` con `decided_by_user_id` y `decided_at`, y registra el evento de auditoría. La cita permanece `APPROVED`. No se crea una cita nueva ni se destruye la anterior.
- **Rechazar (RN-09, RN-10):** libera la retención de la nueva franja (`reschedule_request_id = NULL`), deja intacta la cita original con su franja, sus slots y su estado `APPROVED`, marca la solicitud `REJECTED` con motivo, decisor y fecha, y registra el evento de auditoría. **No** se cancela la cita original ni se fuerza ninguna acción posterior del USER; `patient_action_after_rejection` queda `NULL` porque esa decisión del paciente está fuera del alcance de HU-021.
- Las dos liberaciones son **dirigidas**: la de la cita se aplica por `appointment_id` y la de la retención por `reschedule_request_id`. Ninguna operación libera indiscriminadamente todos los slots de la cita.
- Respuesta `200 application/json` con el mismo objeto de la bandeja, ya actualizado.
- **Auditoría (RN-11, RF-19, CA-03):** cada decisión inserta un evento en `appointment_status_history` con el estado resultante de la cita, actor ADMIN, fuente `ADMIN`, fecha/hora del servidor y el motivo aplicable, además de persistir decisor, fecha y motivo en la propia solicitud.

### Errores de reprogramación

`application/problem+json` con la forma existente `status`, `title` y `detail`.

| HTTP | Caso |
|---|---|
| `400` | `{id}` no válido; `startAt` ausente, mal formado, pasado o no alineado a 30 minutos; `professionalId` o `specialtyId` distintos de los de la cita original; `decision` ausente o desconocida; `REJECT` sin motivo; `status` desconocido en la bandeja. |
| `401` | No hay autenticación válida. |
| `403` | El principal no tiene el rol requerido: USER intentando la bandeja o la decisión ADMIN, ADMIN o PROFESSIONAL intentando solicitar sobre una cita ajena. |
| `404` | La cita no existe o no pertenece al usuario autenticado; la solicitud no existe. Misma respuesta en ambos casos para no revelar existencia ajena. Sin cambios. |
| `409` | La cita no es elegible (no `APPROVED`, pasada o terminal); ya existe una solicitud `PENDING` para esa cita; la nueva franja está ocupada, incompleta o se solapa con la original; la solicitud ya no está `PENDING` al decidir. Sin cambios. |

Ningún caso de error deja cambios parciales: la solicitud, la retención de slots, la actualización de la cita y la auditoría ocurren en la misma transacción y revierten juntas (HU-020 CA-03).

### Decisiones de implementación asociadas a la reprogramación

- Migración nueva `V7__add_reschedule_requests.sql`, **idempotente**, que materializa `reschedule_request_statuses` con su seed, `reschedule_requests` con la forma del esquema de referencia y la columna aprobada `professional_slots.reschedule_request_id` con su clave foránea e índice. Debe ser idempotente porque los volúmenes creados desde `database/reference/db.sql` ya contienen las dos tablas y aplican `V5` en adelante sobre la baseline `4` (DEC-001); la columna se añade de forma condicional consultando `information_schema`.
- La columna `professional_slots.reschedule_request_id` es una **extensión deliberada** sobre `database/reference/db.sql`, aprobada por el usuario el 2026-09-29 para hacer explícita la retención provisional. En la línea de migraciones V1–V6 los identificadores de usuario son `BIGINT` con signo, mientras el esquema de referencia usa `BIGINT UNSIGNED`; `reschedule_requests` se crea con el tipo que corresponda a cada volumen y `CREATE TABLE IF NOT EXISTS` evita tocar el volumen de referencia.
- La autorización añade `/api/v1/admin/reschedule-requests/**` al bloque `ADMIN` ya existente; la ruta de solicitud queda cubierta por el matcher `/api/v1/appointments/**` de `USER`.
- El puerto de persistencia se amplía con operaciones de bloqueo de cita elegible, detección de solicitud `PENDING`, creación de solicitud, liberación de slots por rango, actualización de la franja de la cita y decisión de la solicitud. El dominio y los casos de uso no dependen de HTTP ni de JPA/JDBC.
- No se implementa expiración de retenciones, cambio de profesional, cancelación automática tras rechazo ni acción del paciente posterior al rechazo.

## HU-022 / HU-023 — agenda profesional y cierre de atención

Estado: **Aprobado por el usuario el 2026-09-29.** Conserva el formato existente: IDs como cadenas y fecha/hora local ISO-8601 sin offset en `America/Bogota`. Reglas de referencia: RF-16, RF-17, RN-11, más DEC-006 (criterio de "pasada/aplicable").

| Método y ruta | Rol | Operación |
|---|---|---|
| `GET /api/v1/professional/agenda?range&date&locationId` | PROFESSIONAL | Agenda propia de citas `APPROVED` por día o semana, con sede opcional |
| `POST /api/v1/appointments/{id}/closure` | PROFESSIONAL | Marca una cita propia elegible como `COMPLETED` o `NO_SHOW` |

### Consultar agenda propia

`GET /api/v1/professional/agenda?range={day|week}&date={yyyy-MM-dd}&locationId={id}` — solo `PROFESSIONAL` autenticado.

- `range` es obligatorio: `day` limita al día indicado; `week` limita a la semana de lunes a domingo que contiene esa fecha (calculada en `America/Bogota`). Un valor distinto es `400`.
- `date` es obligatorio y ancla el periodo consultado.
- `locationId` es opcional; cuando se envía, limita el resultado a esa sede.
- El profesional se resuelve exclusivamente del principal JWT (`professionals.user_id`), nunca de un parámetro del cliente. Si el usuario autenticado con rol `PROFESSIONAL` no tiene un registro de profesional asociado, la respuesta es `404` sin exponer detalle adicional.
- El resultado se limita siempre a citas cuyo `professional_id` es el del profesional autenticado y cuyo estado es `APPROVED`; ningún otro estado se incluye. No se expone agenda ajena.
- Proyección minimizada: no se incluye email, documento, teléfono ni afiliación EPS del paciente; solo el nombre necesario para operar la atención.
- Orden estable por `startAt` ascendente y, en empate, `id` ascendente. Sin paginación.
- Respuesta `200 application/json`:

```json
{
  "items": [
    {
      "id": "84",
      "patientName": "Paciente Sintético",
      "location": { "id": "1", "name": "Hospital Internacional de Colombia (HIC)" },
      "specialty": { "id": "1", "name": "Medicina General" },
      "startAt": "2026-10-01T09:00:00",
      "endAt": "2026-10-01T09:30:00",
      "durationMinutes": 30,
      "status": "APPROVED",
      "closureAllowed": false
    }
  ]
}
```

- `closureAllowed` informa si, al momento de construir la respuesta, `endAt` ya no es posterior a la hora actual de `America/Bogota` (DEC-006). Es una ayuda de presentación; el endpoint de cierre vuelve a validar todo bajo bloqueo transaccional.
- Sin coincidencias: `200` con `items: []`.

### Cerrar atención

`POST /api/v1/appointments/{id}/closure` — solo `PROFESSIONAL` autenticado.

```json
{ "outcome": "COMPLETED" }
```

- `outcome` es obligatorio y admite únicamente `COMPLETED` o `NO_SHOW`; cualquier otro valor (incluidos otros códigos del catálogo de estados) es `400` y no produce ningún cambio.
- El profesional propietario se resuelve del principal JWT, igual que en la consulta de agenda.
- **Elegibilidad (DEC-006):** la cita debe pertenecer al profesional autenticado, su estado actual debe ser `APPROVED` y su `endAt` no debe ser posterior a la hora actual de `America/Bogota` — es decir, la cita ya terminó por completo. No hay ventana de tolerancia y el criterio es el mismo para citas de 30 y 60 minutos.
- La transición es explícita y atómica bajo bloqueo de la cita: el estado pasa a `COMPLETED` o `NO_SHOW` según `outcome`, y se inserta un evento en `appointment_status_history` con el estado resultante, actor PROFESSIONAL autenticado (el `user_id` de ese profesional), fuente `USER` y fecha/hora del servidor. El catálogo `change_source` de la referencia solo admite `SYSTEM`, `USER` o `ADMIN` (DEC-001); una acción humana que no es `ADMIN` se registra como `USER`, igual que la cancelación por USER. No se admite motivo proporcionado por el cliente ni edición posterior del resultado.
- Éxito: `200 application/json` con el objeto de agenda actualizado, mismo formato que la consulta, con `status` en el resultado elegido y `closureAllowed: false`.
- Repetir la solicitud sobre una cita ya cerrada, o intentarlo sobre una cita futura, devuelve `409` sin cambiar cita ni historial.

### Errores de agenda/cierre

`application/problem+json` con la forma existente `status`, `title` y `detail`.

| HTTP | Caso |
|---|---|
| `400` | `range` distinto de `day`/`week`; `date` ausente o mal formado; `outcome` ausente o distinto de `COMPLETED`/`NO_SHOW`; `{id}` no válido. |
| `401` | No hay autenticación válida. |
| `403` | El principal autenticado no tiene rol `PROFESSIONAL`. |
| `404` | La cita no existe o no pertenece al profesional autenticado (misma respuesta en ambos casos, sin revelar existencia ajena); o el usuario `PROFESSIONAL` autenticado no tiene registro de profesional asociado. Sin cambios. |
| `409` | La cita propia no es elegible para cierre por ser futura, no estar `APPROVED` o haber cambiado concurrentemente de estado. Sin cambios. |

### Decisiones de implementación asociadas

- La autorización añade `/api/v1/appointments/*/closure` (más específica, evaluada antes) y `/api/v1/professional/**` al rol `PROFESSIONAL`; el resto de `/api/v1/appointments/**` sigue exigiendo `USER`.
- El puerto de persistencia se amplía con la resolución de `professionals.id` a partir del `user_id` autenticado, la consulta de agenda por rango/sede y el bloqueo/cierre de una cita propia. El dominio y los casos de uso no dependen de HTTP ni de JPA/JDBC.
- No se implementa historia clínica, diagnóstico, tratamiento ni ningún criterio temporal distinto al aprobado en DEC-006.

## HU-024 / HU-025 — bandeja administrativa y auditoría de estados

Estado: **aprobado — DEC-007 (2026-10-02)**. Esta sección define únicamente consultas. No crea endpoints de edición o eliminación de auditoría.

Zona horaria: `America/Bogota`. Las fechas y horas se intercambian como valores locales ISO-8601 sin offset. Los identificadores de la respuesta JSON se serializan como cadenas.

### Bandeja administrativa

La bandeja se compone de dos consultas independientes para que cada colección conserve su semántica de estado. La UI puede ejecutarlas en paralelo y presentar un único módulo.

| Método y ruta | Rol | Estado consultado |
|---|---|---|
| `GET /api/v1/admin/appointments?locationId&professionalId&specialtyId&date` | ADMIN | `REQUESTED` |
| `GET /api/v1/admin/reschedule-requests?locationId&professionalId&specialtyId&date` | ADMIN | `PENDING` por omisión |

#### Filtros

- Todos los filtros son opcionales y se combinan con `AND`.
- `locationId`, `professionalId` y `specialtyId` son identificadores positivos.
- `date` usa `yyyy-MM-dd` y compara la fecha local de la franja relevante en `America/Bogota`.
- En solicitudes especializadas, `date` compara `startAt` de la cita.
- En reprogramaciones, `date` compara `requestedStartAt`, es decir, la franja propuesta.
- La consulta de solicitudes especializadas solo admite el estado `REQUESTED`; si se conserva el parámetro histórico `status`, cualquier valor distinto de `REQUESTED` devuelve `400`.
- La consulta de reprogramaciones usa `PENDING` cuando no se envía `status`; los estados históricos que ya soporte el endpoint no forman parte de la bandeja HU-024 y la UI no los solicita.
- No se agregan parámetros de paginación, ordenamiento, búsqueda libre ni selección de propietario.

#### Respuesta de solicitudes especializadas

`200 application/json`:

```json
{
  "items": [
    {
      "id": "84",
      "status": "REQUESTED",
      "patientName": "Paciente Sintético",
      "professional": { "id": "12", "name": "Dra. Ejemplo" },
      "specialty": { "id": "3", "name": "Cardiología" },
      "location": { "id": "1", "name": "Hospital Internacional de Colombia (HIC)" },
      "durationMinutes": 60,
      "startAt": "2026-10-01T09:00:00",
      "endAt": "2026-10-01T10:00:00"
    }
  ]
}
```

#### Respuesta de reprogramaciones

`200 application/json`:

```json
{
  "items": [
    {
      "id": "5",
      "appointmentId": "84",
      "status": "PENDING",
      "patientName": "Paciente Sintético",
      "professional": { "id": "12", "name": "Dra. Ejemplo" },
      "specialty": { "id": "3", "name": "Cardiología" },
      "location": { "id": "1", "name": "Hospital Internacional de Colombia (HIC)" },
      "durationMinutes": 60,
      "previousStartAt": "2026-10-01T09:00:00",
      "previousEndAt": "2026-10-01T10:00:00",
      "requestedStartAt": "2026-10-03T14:00:00",
      "requestedEndAt": "2026-10-03T15:00:00",
      "decisionReason": null,
      "decidedAt": null
    }
  ]
}
```

Sin coincidencias, ambas rutas devuelven `200` con `items: []`. La respuesta no autoriza decidir desde esta HU; las decisiones continúan usando los endpoints existentes de HU-017/HU-021.

Errores de la bandeja: `401` sin autenticación, `403` para cualquier rol distinto de `ADMIN`, `400` para identificadores no positivos, fecha inválida o estado no permitido. No se revela información de la bandeja a roles no ADMIN.

### Consulta de auditoría de estados

| Método y ruta | Roles autorizados por la matriz propuesta | Operación |
|---|---|---|
| `GET /api/v1/appointments/{appointmentId}/status-history` | ADMIN, USER propietario, PROFESSIONAL de la cita | Consulta de solo lectura |

La ruta devuelve únicamente eventos de la cita indicada. `appointmentId` debe ser positivo.

`200 application/json`:

```json
{
  "items": [
    {
      "id": "901",
      "appointmentId": "84",
      "status": "REQUESTED",
      "actor": { "id": "27", "name": "Paciente Sintético" },
      "source": "USER",
      "changedAt": "2026-09-29T14:05:00",
      "reason": "Solicitud especializada creada"
    },
    {
      "id": "902",
      "appointmentId": "84",
      "status": "REJECTED",
      "actor": { "id": "1", "name": "Administrador Sintético" },
      "source": "ADMIN",
      "changedAt": "2026-09-29T15:10:00",
      "reason": "El profesional no estará disponible."
    }
  ]
}
```

Campos:

- `status` es el estado nuevo de la cita.
- `actor` es `null` cuando la fuente es `SYSTEM`; cuando existe actor se expone únicamente su identificador y nombre necesario para la trazabilidad.
- `source` solo admite `SYSTEM`, `USER` o `ADMIN`.
- `changedAt` es la fecha/hora del servidor.
- `reason` es opcional y puede ser `null`.

Sin eventos, la ruta devuelve `200` con `items: []`. La API no promete paginación ni una garantía nueva de orden; el cliente no debe depender de un orden distinto al entregado por la consulta.

#### Matriz de visibilidad — aprobada (DEC-007)

| Rol | Puede consultar auditoría | Alcance |
|---|---|---|
| ADMIN | Sí | Cualquier cita |
| USER | Sí | Solo citas cuyo `patient_user_id` coincide con el principal autenticado |
| PROFESSIONAL | Sí | Solo citas cuyo profesional corresponde al principal autenticado |
| Cualquier otro rol o sin rol habilitado | No | Denegado |

Para un rol habilitado que intenta consultar una cita fuera de su alcance, la API devuelve `404` sin revelar si la cita existe. Sin autenticación devuelve `401`; un rol no contemplado en la matriz devuelve `403`.

No existen ni se publican `POST`, `PUT`, `PATCH` o `DELETE` para `appointment_status_history`. La auditoría se escribe únicamente como efecto de transiciones de estado y no se modifica como CRUD normal.

#### Decisiones de implementación (confirmadas por DEC-007)

- La regla de autorización de `/api/v1/appointments/{appointmentId}/status-history` debe evaluarse antes del matcher genérico de citas USER.
- La consulta de auditoría debe hacer cumplir ownership de USER/PROFESSIONAL en el caso de uso o adaptador autorizado, no solo ocultar controles en la UI.
- Las respuestas de error conservan `application/problem+json` con `status`, `title` y `detail`.
- La interfaz deberá comunicar `loading`, `empty`, `error` y `success`; esos estados no agregan rutas ni permisos al contrato.
