---
id: HU-001
tipo: historia-de-usuario
titulo: "Consultar catálogos fijos"
estado: Pendiente de aprobación
epica: "[[EP-001-acceso-perfil-y-catalogos-fijos]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 1"
dependencias: []
relacionadas: ["[[HU-002-registrar-usuario]]", "[[HU-008-gestionar-especialidades]]"]
---
# HU-001 — Consultar catálogos fijos
## Historia de usuario
**COMO** persona usuaria del sistema **QUIERO** consultar roles, estados de cita/reprogramación, regímenes y sedes fijas **PARA** seleccionar valores válidos sin datos libres.
## Alcance
- Seed y lectura autorizada de catálogos fijos; sedes HIC e ICV con los datos públicos provistos.
## Fuera de alcance
- CRUD de estos catálogos y catálogos configurables.
## Reglas de negocio
- Son de solo lectura y el modelo debe conservar 3FN.
## Dependencias y relaciones
- Épica: [[EP-001-acceso-perfil-y-catalogos-fijos]]; relacionadas: [[HU-002-registrar-usuario]], [[HU-008-gestionar-especialidades]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** datos semilla, integridad referencial y contrato de lectura.
## Tareas de desarrollo
- [ ] **T-01 — Modelar catálogos normalizados.** Dificultad: Medio. Definir claves, relaciones y migración Flyway propia.
- [ ] **T-02 — Publicar lectura protegida según contrato.** Dificultad: Medio. Aplicar validación, autorización y pruebas.
- [ ] **T-03 — Exponer consumo web.** Dificultad: Bajo. Integrar solo tras contrato/diseño aprobados.
## Criterios de aceptación
### CA-01 — Datos fijos disponibles
**Dado** un entorno inicializado **Cuando** se consultan los catálogos fijos **Entonces** se obtienen roles, estados, regímenes y las dos sedes definidas sin opción de edición.
### CA-02 — Integridad de catálogo
**Dado** una operación que necesita un valor de catálogo **Cuando** recibe un identificador inexistente/inactivo no aplicable **Entonces** es rechazada con error contractual sin crear datos libres.
## Definition of Done
- [ ] CA-01 y CA-02 validados con evidencia.
- [ ] Migración Flyway, seed e integridad 3FN verificados.
- [ ] Contrato REST y pruebas de persistencia/lectura aplicables registrados.
- [ ] Trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
## Notas y decisiones
- La representación concreta de IDs y errores queda para [[HU-026-documentar-contrato-rest-base]].
