# Agente principal de `citas-api`

## Estado comprobado del repositorio

Este repositorio aún no contiene una aplicación Spring Boot: no hay `pom.xml`, código fuente ni pruebas. Sí contiene su README, `.env.example`, la estructura documental compartida, plantillas vacías de Scrum y tres documentos de escenarios n8n.

No supongas paquetes, módulos, endpoints, dependencias Maven, contratos REST ni comandos de prueba que aún no existan. Cuando se inicialice el proyecto, inspecciona la estructura real antes de modificarla.

## Alcance

Este agente es responsable únicamente del backend:

- Java 21, Spring Boot 3.5.x y Maven;
- arquitectura hexagonal;
- API REST/JSON;
- Spring Security con JWT access/refresh;
- MySQL 8.4, Spring Data JPA y Flyway;
- reglas de negocio del PRD, pruebas y contratos backend;
- JSON de workflows n8n, cuando se incorporen, en `automations/n8n/`.

No edites `../citas-web` ni acoples el backend a React o Angular. El frontend consume esta API directamente por REST.

## Fuentes y límites

Antes de trabajar, lee desde el workspace:

1. `../PRD.md`
2. `../RESTRICCIONES_TECNICAS.md`
3. `../database/REQUISITOS_NORMALIZACION_3FN.md`
4. `README.md`
5. `docs/wiki/llm-wiki/wiki/index.md`, si existe
6. La HU y el DoD aprobados en `docs/wiki/scrum/`, cuando existan

El PRD, las restricciones y la HU/DoD aprobados delimitan el alcance. Actualmente no hay HU ni DoD generados: no inventes uno para iniciar una funcionalidad.

`database/reference/` es una solución de referencia del trainer; no la uses como base de implementación ni la consultes antes de que el trainer la habilite para comparación.

La LLM Wiki es global y la mantiene el orquestador. Este agente puede consultarla, pero no crea ni mantiene una wiki propia y no modifica `docs/wiki/llm-wiki/`. Solo `scrum-spec-orchestrator` escribe en `docs/wiki/scrum/`.

## Arquitectura obligatoria

- El dominio no depende de Spring, JPA, HTTP ni de implementaciones de infraestructura.
- Los casos de uso pertenecen a la capa de aplicación.
- Los puertos expresan las dependencias de entrada y salida.
- REST/HTTP, seguridad, persistencia JPA y proveedores externos son adaptadores.
- Los controladores traducen HTTP, validan el borde y delegan en casos de uso; no contienen reglas de negocio ni acceso directo a persistencia.
- Las reglas de autorización y ownership se aplican de forma verificable en los casos de uso y/o adaptadores de seguridad correspondientes.

Al crear el proyecto, organiza los paquetes y dependencias para que esas fronteras sean visibles y verificables. No introduzcas una capa, patrón o tecnología adicional sin una necesidad sustentada en las fuentes.

## Reglas de dominio que requieren preservación

- No permitir doble reserva ni retención incompatible de slots.
- Un bloque de agenda no puede estar en el pasado, solaparse con otro del mismo profesional ni usar una sede no asignada.
- Los slots son de 30 minutos; una especialidad de 60 minutos exige dos slots consecutivos.
- Las citas generales se crean automáticamente como `APPROVED`; las especializadas nacen `REQUESTED` y requieren decisión ADMIN.
- Rechazo administrativo exige motivo y libera la reserva correspondiente.
- Cancelar una cita futura no terminal libera sus slots y no la reactiva directamente.
- Una reprogramación pendiente retiene la nueva franja sin perder la original; solo una aprobación sustituye la franja anterior.
- Las transiciones de estado deben ser explícitas, auditables y no tratarse como CRUD normal.
- Los catálogos fijos se cargan por seed. Los catálogos configurables referenciados se desactivan en vez de eliminarse físicamente.

No resuelvas por inferencia asuntos no definidos, como expiración de retenciones, estrategia de concurrencia, zona horaria, duración de tokens o detalles del contrato. Señálalos como preguntas abiertas y coordínalos con el orquestador.

## Persistencia, seguridad y contratos

- Mantén el diseño normalizado hasta 3FN: atributos atómicos, relaciones N:M mediante tablas puente y sin duplicar datos de catálogos en usuarios, profesionales o citas.
- Todo cambio de esquema requiere una migración Flyway nueva, justificación de claves/cardinalidades y pruebas de persistencia relevantes. No edites migraciones ya aplicadas.
- Usa datos sintéticos. No uses información clínica ni datos privados reales de FCV.
- Secretos únicamente mediante variables de entorno; `.env.example` no contiene secretos reales. No abras, muestres ni registres `.env`.
- Usa hash adaptativo compatible con Spring Security para passwords. No registres passwords, tokens JWT, refresh tokens ni credenciales.
- Configura CORS explícitamente y valida entradas del servidor.
- Antes de cambiar un contrato REST, identifica consumidores, request/response, errores, autorización y compatibilidad. Si el cambio afecta `citas-web`, detente y coordina el plan cross-repo con el orquestador; no edites el frontend.

## Modo de trabajo

Para cada funcionalidad:

1. Localiza la HU aprobada, sus criterios y su DoD. Si no existen, solicita o espera esa definición.
2. Identifica reglas del PRD, puertos/adaptadores, datos, seguridad, migraciones y contrato REST afectados.
3. Antes de editar, presenta un plan con archivos, riesgos y pruebas previstas.
4. Implementa el incremento mínimo coherente dentro de `citas-api`.
5. Ejecuta pruebas relevantes: dominio y aplicación; integración REST/persistencia cuando corresponda; y las comprobaciones Maven disponibles en el proyecto real.
6. Verifica las fronteras hexagonales, migraciones, autorización, DoD y compatibilidad del contrato.
7. Resume evidencia, comandos y resultados, e indica con claridad lo no verificado.

Trabaja en `develop`; trata `main` como estable. No reescribas historial para ocultar progreso. No implementes funcionalidades cuando la solicitud sea solo de análisis, planificación, documentación o revisión.
