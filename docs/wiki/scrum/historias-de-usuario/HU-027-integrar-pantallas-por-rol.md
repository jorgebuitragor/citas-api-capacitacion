---
id: HU-027
tipo: historia-de-usuario
titulo: "Integrar pantallas obligatorias por rol"
estado: Pendiente de aprobación
epica: "[[EP-009-contrato-rest-y-experiencia-web]]"
esfuerzo: Muy alto
sprint_sugerido: "Incremento 5"
dependencias: ["[[HU-026-documentar-contrato-rest-base]]"]
relacionadas: ["[[HU-018-consultar-mis-citas]]", "[[HU-022-consultar-agenda-profesional]]", "[[HU-024-consultar-bandeja-administrativa]]"]
---
# HU-027 — Integrar pantallas obligatorias por rol
## Historia de usuario
**COMO** usuario de cada rol **QUIERO** acceder a las pantallas obligatorias conectadas a la API **PARA** completar mis flujos con una experiencia visual aprobada.
## Alcance
- Rutas/pantallas PRD: acceso, perfil, búsqueda/reserva, mis citas/reprogramación, profesional y ADMIN; consumo REST directo.
## Fuera de alcance
- Inventar React/Angular, estética o contratos antes de Stitch/AI Studio y HU funcional aprobadas.
## Reglas de negocio
- Diseño aprobado es fuente visual; loading/empty/error/success/disabled aplicables; backend autoriza definitivamente.
## Dependencias y relaciones
- Épica: [[EP-009-contrato-rest-y-experiencia-web]]; depende de [[HU-026-documentar-contrato-rest-base]] y de cada HU funcional integrada.
## Esfuerzo
**Nivel:** Muy alto. **Justificación:** abarca múltiples flujos y debe dividirse por pantallas/HU aprobadas durante implementación.
## Tareas de desarrollo
- [ ] **T-01 — Obtener/aprobar diseño Stitch/AI Studio.** Dificultad: Alto. Detectar framework/exportación real.
- [ ] **T-02 — Integrar rutas por flujo.** Dificultad: Alto. Dividir en tareas derivadas por HU y rol.
- [ ] **T-03 — Verificar accesibilidad/contrato.** Dificultad: Alto. Build/typecheck y pruebas disponibles.
## Criterios de aceptación
### CA-01 — Fuente visual
**Dado** un diseño aprobado y contrato aprobado **Cuando** se integra una pantalla **Entonces** respeta ambos sin BFF/Express ni URL hardcodeada.
### CA-02 — Estados y rutas
**Dado** una pantalla implementada **Cuando** recibe carga, vacío, error, éxito o acción deshabilitada aplicable **Entonces** los comunica de forma accesible y evita doble envío.
### CA-03 — Autorización real
**Dado** una ruta de rol **Cuando** se intenta usar sin sesión/rol válido **Entonces** la UI orienta según contrato y el backend conserva la decisión definitiva.
## Definition of Done
- [ ] CA-01 a CA-03 validados para cada pantalla incorporada con evidencia.
- [ ] Framework/diseño real detectados; build/typecheck/pruebas aplicables y validación REST verificados.
- [ ] Esta HU se divide en HUs de interfaz aprobables si mantiene alcance Muy alto.
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
- No es seleccionable para implementación integral sin descomponer tras diseño aprobado.
