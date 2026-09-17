---
id: HU-009
tipo: historia-de-usuario
titulo: "Crear y administrar profesional"
estado: Pendiente de aprobación
epica: "[[EP-003-administracion-de-profesionales]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 2"
dependencias: ["[[HU-003-iniciar-y-cerrar-sesion]]"]
relacionadas: ["[[HU-010-asignar-especialidades-al-profesional]]", "[[HU-011-asignar-sedes-al-profesional]]"]
---
# HU-009 — Crear y administrar profesional
## Historia de usuario
**COMO** ADMIN **QUIERO** crear y activar/desactivar un usuario PROFESSIONAL con código y matrícula ficticia **PARA** habilitar su participación en la oferta de atención.
## Alcance
- Cuenta PROFESSIONAL creada por ADMIN, datos sintéticos, código y matrícula ficticia, estado activo.
## Fuera de alcance
- Registro autónomo del profesional, asignaciones de sede/especialidad.
## Reglas de negocio
- Datos sintéticos; solo ADMIN crea; activo/inactivo condiciona disponibilidad/reserva.
## Dependencias y relaciones
- Épica: [[EP-003-administracion-de-profesionales]]; depende de [[HU-003-iniciar-y-cerrar-sesion]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** identidad, rol, unicidad y efectos sobre capacidades posteriores.
## Tareas de desarrollo
- [ ] **T-01 — Modelar extensión profesional.** Dificultad: Alto. Relación usuario-profesional, claves únicas y migración.
- [ ] **T-02 — Implementar administración.** Dificultad: Alto. Casos de uso, autorización y validaciones.
- [ ] **T-03 — Integrar CRUD ADMIN.** Dificultad: Medio. Formularios aprobados y estados de lista.
## Criterios de aceptación
### CA-01 — Alta administrativa
**Dado** un ADMIN autenticado **Cuando** registra datos profesionales ficticios válidos **Entonces** se crea una cuenta PROFESSIONAL con código y matrícula.
### CA-02 — Activación operativa
**Dado** un profesional existente **Cuando** ADMIN lo activa o desactiva **Entonces** su estado se actualiza y las capacidades dependientes lo respetan.
### CA-03 — Restricción de acceso
**Dado** un rol no ADMIN **Cuando** intenta crear o modificar profesionales **Entonces** la operación es denegada.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Migración, unicidad, autorización, pruebas y contrato aplicables verificados.
- [ ] No se introdujeron datos personales reales; trazabilidad Scrum actualizada.
## Evidencia de validación
| Elemento | Resultado | Evidencia | Observación |
|---|---|---|---|
| CA-01 | Pendiente | — | — |
| CA-02 | Pendiente | — | — |
| CA-03 | Pendiente | — | — |
| DoD | Pendiente | — | — |
## Historial de validación
- 2026-09-17 — Creada en estado `Pendiente de aprobación`.
## Notas y decisiones
- Definir qué ocurre con agenda/citas al desactivar requiere decisión de dominio.
