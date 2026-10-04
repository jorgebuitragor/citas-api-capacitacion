# Contrato REST — automatizaciones (HU-028, DEC-010)

Alcance: lo que n8n consume para WF-001. Es una interfaz servidor a servidor: no usa el JWT de ningún usuario.

## Autenticación

Cabecera `X-Api-Key` con el valor de la variable de entorno `AUTOMATION_API_KEY`. Si la variable está vacía el acceso queda **deshabilitado** (siempre `401`). La clave se compara en tiempo constante, nunca se registra en logs y no se versiona (`.env.example` solo trae `CHANGE_ME`). La clave otorga únicamente el rol `AUTOMATION`, que solo accede a `/api/v1/automation/**`; no sirve para ninguna otra ruta. Sin clave o con clave incorrecta: `401`.

## Citas próximas

`GET /api/v1/automation/upcoming-appointments?hours=24`

- `hours`: entero entre 1 y 72 (por defecto 24); otro valor responde `400`.
- Devuelve las citas `APPROVED` cuya franja empieza después de ahora y hasta `ahora + hours` (zona `America/Bogota`), **sin** un recordatorio `SENT` registrado para esa misma franja. Nunca incluye `REQUESTED`, `REJECTED`, `CANCELLED`, `COMPLETED` ni `NO_SHOW`.

```json
{
  "windowHours": 24,
  "items": [
    {
      "appointmentId": "84",
      "startAt": "2026-10-05T09:00:00",
      "endAt": "2026-10-05T09:30:00",
      "patient": { "name": "Paciente Sintético", "email": "paciente@example.test" },
      "professionalName": "Profesional General",
      "specialtyName": "Medicina General",
      "locationName": "Hospital Internacional de Colombia (HIC)"
    }
  ]
}
```

Sin coincidencias: `200` con `items: []`.

## Registrar el resultado

`POST /api/v1/automation/appointments/{appointmentId}/reminders`

```json
{ "status": "SENT", "detail": "Mailpit 250 OK" }
```

- `status`: `SENT` o `FAILED`; `detail` opcional (máx. 500 caracteres; no debe contener secretos).
- `201` con `{ "id", "appointmentId", "status", "recordedAt" }`.
- La franja se toma del servidor (no del cliente). Un segundo `SENT` para la misma cita y franja responde `409` (deduplicación); `FAILED` puede repetirse y **no** bloquea un reintento.
- Cita inexistente o que ya no está `APPROVED`: `404`.

## Deduplicación

Una fila `SENT` por `(cita, franja)`. Si la cita se reprograma, su franja cambia y vuelve a ser elegible. Un fallo (`FAILED`) deja rastro pero no impide reintentar.

## Errores

`application/problem+json` como el resto de la API: `400` parámetro inválido, `401` clave ausente o incorrecta, `404`, `409`.
