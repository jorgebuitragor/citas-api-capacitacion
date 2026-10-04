# WF-001 — Recordatorio de citas próximas

**Trigger:** Schedule.

**Objetivo:** consultar citas `APPROVED` dentro de una ventana configurable (ej. próximas 24 h), enviar Gmail al usuario ficticio/de laboratorio y registrar resultado.

## Requisitos
- no enviar recordatorio a CANCELLED/REJECTED;
- evitar duplicado para la misma cita/ventana según estrategia del estudiante;
- manejar API no disponible;
- credenciales fuera del JSON;
- ejecución de prueba controlada antes de activar.

## Entregable
`WF-001-appointment-reminders.json`.

## Diseño aprobado (DEC-010)

`Cada hora → Configuración → Consultar citas próximas → Una cita por ítem → Enviar (SMTP/Mailpit) → Registrar SENT | Registrar FAILED`, con la rama `API no disponible` en la consulta.

- **Ventana:** próximas 24 h (`windowHours` en el nodo *Configuración*; la API acepta 1–72).
- **Destinatario:** `patient.email` (sintético) devuelto por la API.
- **Deduplicación:** la API excluye las citas con un `SENT` para su franja actual; el workflow registra el resultado con `POST .../reminders`. Un `FAILED` no bloquea el reintento de la siguiente ejecución horaria; una reprogramación vuelve a hacer elegible la cita.
- **Fuente REST:** `GET /api/v1/automation/upcoming-appointments` (contrato en `docs/contracts/automation.md`), solo citas `APPROVED`.
- **API no disponible:** la consulta reintenta 3 veces; si sigue fallando, la rama de error no envía ni registra nada y la próxima ejecución reintenta.

## Configuración externa (no versionada)

| Credencial en n8n | Tipo | Valor |
|---|---|---|
| `Citas API key` | Header Auth | nombre `X-Api-Key`, valor = `AUTOMATION_API_KEY` del `.env` de la API |
| `Mailpit SMTP (laboratorio)` | SMTP | host del Mailpit alcanzable desde n8n, puerto 1025, sin TLS ni usuario |
| `Gmail OAuth2` (opcional) | Gmail OAuth2 | la crea cada persona con su proyecto de Google Cloud; el nodo Gmail viene desactivado |

`apiBaseUrl` en *Configuración* debe ser alcanzable **desde n8n** (si n8n es remoto, hace falta un túnel o una URL pública del laboratorio). El JSON se importa con `active: false`; no activarlo hasta validar la salida con una ejecución manual.

## Estado

JSON generado y validado estructuralmente; **aún no importado ni ejecutado en n8n**. Ver `docs/evidence/goals-loops/S5/S5-01-WF001-iteracion-1.md`.

