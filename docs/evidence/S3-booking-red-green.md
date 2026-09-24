# S3 — evidencia Red → Green de reservas

## Red

- Fecha: 2026-09-24.
- Prueba creada antes del endpoint: `BookingIntegrationTest.rejectsSecondReservationWithoutCreatingAnotherAppointment`.
- Comando: `docker compose exec -T citas-api-dev mvn -Dtest=BookingIntegrationTest test`.
- Resultado inicial: error rojo `Table 'citas_fcv_training.professional_slots' doesn't exist`.
- Hallazgo: el MySQL activo estaba en Flyway V2 y no había materializado el esquema de agenda de referencia. No se creó ninguna cita parcial.

## Green

- Corrección: migraciones `V3__add_scheduling_schema.sql` y `V4__seed_scheduling_demo_data.sql`; reserva transaccional con bloqueo pesimista.
- Compatibilidad: un volumen nuevo inicializado desde `database/reference/db.sql` registra baseline Flyway `4` y omite V1–V4, que únicamente corrigen el volumen legado que ya estaba en V2.
- Comando: `docker compose exec -T citas-api-dev mvn test`.
- Resultado: `AuthIntegrationTest` 5/5 y `BookingIntegrationTest` 5/5, sin fallos ni errores.
- La prueba verde demuestra que la segunda reserva responde `409` y que el conteo de citas aumenta solo una vez.

## Checkpoints

1. Contrato y DEC-003 fijados: hora Bogotá, retención hasta decisión y bloqueo pesimista.
2. Migración del esquema de agenda aplicada a MySQL desde V2 a V4.
3. Backend y frontend cubiertos por pruebas, build y hooks locales.

## Hooks

- `citas-web/.githooks/pre-commit`: bloqueó un archivo temporal staged con patrón `sk-…`; tras retirarlo ejecutó typecheck, 2 pruebas Vitest y build en verde.
- `citas-api/.githooks/pre-commit`: bloqueó el mismo patrón temporal; tras retirarlo ejecutó `mvn test` en verde: 10 pruebas, 0 fallos y 0 errores.
- El fixture temporal se eliminó antes de conservar los cambios.
