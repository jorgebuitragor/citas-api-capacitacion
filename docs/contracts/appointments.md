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
