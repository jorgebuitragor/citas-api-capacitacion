# S5.01 — WF-001 recordatorios: iteración 1 (parcial, pausada en n8n/MCP)

- **Fecha:** 2026-10-04.
- **Estado:** **PARCIAL.** La API, el contrato, el workflow exportado y la entrega por SMTP están verificados; la **ejecución en n8n, la inspección por MCP y el envío por Gmail NO se han hecho** (dependen del n8n del trainer y de credenciales del usuario). Por eso HU-028 sigue sin marcarse `Completada`.
- **Decisiones:** DEC-010 (ventana 24 h, destinatario = email sintético del paciente, deduplicación por cita y franja, fuente REST nueva con API key de servicio; n8n del trainer; Mailpit + nodo Gmail listo).

## Qué se construyó

| Pieza | Dónde |
|---|---|
| Endpoint de lectura de citas próximas y de registro de resultado | `citas-api`: `AutomationController`, módulo `application/automation`, `AutomationPersistenceAdapter` |
| Credencial de servicio (`X-Api-Key`, solo rol `AUTOMATION`, solo `/api/v1/automation/**`, comparación en tiempo constante, deshabilitada si la clave está vacía) | `ApiKeyAuthenticationFilter`, `SecurityConfiguration` |
| Rastro y deduplicación | migración `V10__add_appointment_reminders.sql` (`SENT` único por cita y franja; `FAILED` no bloquea) |
| Contrato | `docs/contracts/automation.md` |
| Workflow importable sin secretos, `active: false` | `automations/n8n/WF-001-appointment-reminders.json` |
| SMTP de laboratorio opcional y variable de la clave | `docker-compose.yml` (perfil `automation`), `.env.example` |

No se modificó el núcleo de reservas: el único cambio en `citas-api` es este módulo nuevo de solo lectura más el registro de resultados.

## Verificación realizada

**1. Pruebas automatizadas** — backend 49/49 (43 previas + 6 nuevas en `AutomationIntegrationTest`): exige clave y no concede nada más (la clave no sirve en `/admin` ni `/appointments`; un JWT ADMIN recibe `403`); valida `hours` (0, 73, -5 → `400`); selecciona solo `APPROVED` dentro de la ventana (excluye `REQUESTED`, `CANCELLED`, `REJECTED`, `COMPLETED`, fuera de ventana y pasadas); omite pacientes inactivos; deduplica `SENT` por cita y franja y permite reintento tras `FAILED`; rechaza recordatorios para citas no aprobadas, inexistentes, estado inválido y `detail` > 500.

**2. Réplica del workflow contra la API real y Mailpit** (script que repite nodo a nodo lo que hará n8n; **no es una ejecución en n8n**), 9/9:

| Caso | Resultado |
|---|---|
| Sin clave / clave errónea / clave correcta | `401` / `401` / `200` |
| Primera ejecución con 4 citas (APPROVED en 3 h, APPROVED en 30 h, REQUESTED, CANCELLED) | procesa solo la elegible: correo enviado y `SENT` (`201`) |
| Segunda ejecución | no reenvía (deduplicación) |
| SMTP caído | registra `FAILED`; en la siguiente ejecución se reenvía y queda `SENT` |
| API caída | rama de error: no envía ni registra nada |
| Mailpit | exactamente 2 correos, ambos al email sintético, asunto con la especialidad |
| Tabla `appointment_reminders` | `SENT`, `FAILED`, `SENT` en el orden esperado |
| Cita reprogramada | vuelve a ser elegible y recibe un nuevo recordatorio |

**3. JSON del workflow:** válido, 9 nodos con nombres e ids únicos, todas las conexiones apuntan a nodos existentes, `active: false`, credenciales solo como referencias con id vacío, sin patrones de secretos.

## Defectos y riesgos encontrados

- **La suite de reservas dependía de la fecha:** la disponibilidad sintética se siembra "para mañana" respecto del día de la migración, así que al pasar los días no quedaba ninguna franja futura y 20 de 24 pruebas fallaban (no era regresión de este trabajo). `BookingIntegrationTest` ahora reaplica el script idempotente de V6 antes de cada caso. Efecto colateral conocido: en el entorno de laboratorio también se agota la disponibilidad demo con los días.
- **Error propio del script de réplica:** primera versión restaba 5 h a `NOW()`, pero MySQL ya corre con `TZ=America/Bogota` (compose) igual que el reloj de la API; con 0 resultados fue el script, no la API.
- **Riesgo residual — n8n remoto:** `apiBaseUrl` debe ser alcanzable desde el n8n del trainer; un Mailpit local no lo es sin túnel. Sin acceso no se puede verificar la importación ni la ejecución.
- **Riesgo residual — modelo de credencial:** una sola API key compartida (rotación manual, sin expiración, sin limitación de tasa). Aceptable para laboratorio; para producción requeriría rotación y auditoría por llamada.
- **Riesgo residual — datos personales en tránsito:** la API devuelve nombre y email del paciente a quien tenga la clave; en el laboratorio son sintéticos.

## Pendiente para cerrar S5.01 (requiere al usuario)

1. Acceso al n8n del trainer (URL) y cómo está expuesto el MCP; yo no manejo credenciales: las crea el usuario.
2. Una URL de la API alcanzable desde ese n8n y un SMTP/Mailpit alcanzable (o el nodo Gmail con OAuth propio).
3. Importar el JSON, crear las credenciales, ejecutar una vez de forma manual y confirmar la salida; después el ciclo Builder/Verifier del LOOP S5.01.
4. Invocación MCP desde el agente (evidencia de la guía S5) — no verificable sin el punto 1.

**Verifier (resumen de esta iteración):** PASS parcial. Ausencia de credenciales, selección exclusiva de `APPROVED` elegibles, manejo de fallo de API y registro de resultado: verificados. Importabilidad real y ejecución controlada en n8n: **no verificadas**.
