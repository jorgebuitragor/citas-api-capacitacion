---
id: HU-013
tipo: historia-de-usuario
titulo: "Consultar calendario de disponibilidad"
estado: Pendiente de aprobación
epica: "[[EP-004-disponibilidad-del-profesional]]"
esfuerzo: Medio
sprint_sugerido: "Incremento 3"
dependencias: ["[[HU-012-gestionar-bloques-de-disponibilidad]]"]
relacionadas: ["[[HU-022-consultar-agenda-profesional]]"]
---
# HU-013 — Consultar calendario de disponibilidad
## Historia de usuario
**COMO** PROFESSIONAL **QUIERO** consultar mi calendario de bloques **PARA** conocer la disponibilidad que publiqué.
## Alcance
- Vista de bloques propios por fecha/sede según diseño aprobado.
## Fuera de alcance
- Consulta de agendas de otros profesionales o cambio de citas.
## Reglas de negocio
- Ownership del profesional; datos de citas no necesarios para este calendario.
## Dependencias y relaciones
- Épica: [[EP-004-disponibilidad-del-profesional]]; depende de [[HU-012-gestionar-bloques-de-disponibilidad]].
## Esfuerzo
**Nivel:** Medio. **Justificación:** consulta filtrada, ownership y representación calendario.
## Tareas de desarrollo
- [ ] **T-01 — Definir consulta/calendario.** Dificultad: Medio. Filtros y contrato.
- [ ] **T-02 — Aplicar ownership.** Dificultad: Medio. Pruebas contra acceso ajeno.
- [ ] **T-03 — Renderizar estados.** Dificultad: Bajo. Loading/empty/error aprobados.
## Criterios de aceptación
### CA-01 — Calendario propio
**Dado** un PROFESSIONAL autenticado **Cuando** consulta su calendario **Entonces** ve sus bloques con fecha, rango y sede.
### CA-02 — Sin exposición ajena
**Dado** otro profesional **Cuando** intenta consultar o inferir bloques ajenos **Entonces** el servidor lo impide.
### CA-03 — Sin bloques
**Dado** un rango sin bloques propios **Cuando** lo consulto **Entonces** la interfaz presenta estado vacío comprensible.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Ownership, filtros, contrato, pruebas y estados web aplicables verificados.
- [ ] Trazabilidad Scrum actualizada.
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
- Periodo de consulta se define por contrato.
