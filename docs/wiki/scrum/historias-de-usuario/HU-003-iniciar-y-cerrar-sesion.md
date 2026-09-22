---
id: HU-003
tipo: historia-de-usuario
titulo: "Iniciar y cerrar sesión"
estado: Aprobada
epica: "[[EP-001-acceso-perfil-y-catalogos-fijos]]"
esfuerzo: Alto
sprint_sugerido: "Incremento 1"
dependencias: ["[[HU-002-registrar-usuario]]"]
relacionadas: ["[[HU-026-documentar-contrato-rest-base]]"]
---
# HU-003 — Iniciar y cerrar sesión
## Historia de usuario
**COMO** usuario registrado **QUIERO** iniciar sesión, renovar mi acceso y cerrarlo **PARA** usar las capacidades autorizadas de mi rol.
## Alcance
- Login email/password, access/refresh separados, refresh, logout/revocación y contexto de rol.
## Fuera de alcance
- SSO, OAuth y administración de sesiones ajenas.
## Reglas de negocio
- JWT access/refresh separados; autorización por rol; no registrar tokens/credenciales.
## Dependencias y relaciones
- Épica: [[EP-001-acceso-perfil-y-catalogos-fijos]]; depende de [[HU-002-registrar-usuario]]; relacionada: [[HU-026-documentar-contrato-rest-base]].
## Esfuerzo
**Nivel:** Alto. **Justificación:** seguridad transversal y contrato entre ambos repositorios.
## Tareas de desarrollo
- [ ] **T-01 — Definir ciclo de sesión.** Dificultad: Alto. Precisar claims, persistencia/revocación y configuración por entorno.
- [ ] **T-02 — Implementar autenticación/autorización.** Dificultad: Alto. Adaptadores de seguridad y pruebas negativas.
- [ ] **T-03 — Integrar guardas de rutas.** Dificultad: Medio. Consumir contrato sin sustituir controles del servidor.
## Criterios de aceptación
### CA-01 — Credenciales válidas
**Dado** una cuenta activa **Cuando** inicia con email y contraseña correctos **Entonces** recibe access y refresh según contrato con su contexto de rol.
### CA-02 — Renovación y revocación
**Dado** una sesión válida **Cuando** renueva o cierra sesión **Entonces** el refresh se procesa o revoca y no permite usos posteriores a logout.
### CA-03 — Acceso no autorizado
**Dado** token ausente, inválido o rol insuficiente **Cuando** se llama una capacidad protegida **Entonces** se deniega sin revelar secretos.
## Definition of Done
- [ ] CA-01 a CA-03 validados con evidencia.
- [ ] Secrets por environment, CORS explícito, sin logs de tokens y pruebas de seguridad aplicables verificados.
- [ ] Contrato de auth y consumidor web documentados/validados.
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
- 2026-09-17 — Aprobada explícitamente por el usuario para selección e implementación posterior.
- 2026-09-22 — DEC-002: usuario aprueba explícitamente la interfaz gráfica de la pantalla de login (ver `citas-api/docs/wiki/llm-wiki/wiki/decisiones.md`).
## Notas y decisiones
- Duraciones, almacenamiento cliente y semántica de refresh/logout siguen abiertos.
- Interfaz gráfica aprobada (DEC-002); falta demostración end-to-end contra el backend corriendo.
