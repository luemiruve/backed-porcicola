# 🐷 Sistema Porcícola

Backend para gestión integral de una granja porcina: ciclo reproductivo, inventario, sanidad animal y notificaciones automáticas. Multi-tenant por granja (`farm_id`), con control de acceso por rol.

## 🚀 Stack
- Java 21 + Spring Boot 3.5
- PostgreSQL (Supabase) · JPA / Hibernate (`ddl-auto=validate` — la BD es la fuente de verdad del esquema)
- JWT stateless (jjwt) + Spring Security (`@PreAuthorize` por rol)
- Próximo: App Android (Kotlin) · Futuro: App iOS (Swift)

## 🧠 Arquitectura
- REST API en capas: `Controller` → `Service` → `Repository` → `Model`, con `DTO` + `Mapper` por recurso
- Cada request autenticado resuelve un `UserPrincipal` (`farm_id` + rol) vía `CurrentUser.get()`; todo aislamiento multi-tenant se deriva de ahí, nunca de un `farm_id` que venga del cliente
- Respuestas envueltas en `ApiResponse<T>` estándar

## 📦 Funcionalidades implementadas
- **Auth**: registro/login con JWT (`AuthController`)
- **Granjas**: CRUD (`FarmController`)
- **Usuarios**: gestión por granja, roles `ADMIN`/`WORKER` (`UserController`)
- **Animales**: CRUD + filtros por tipo/estado, aislamiento por granja, validación de madre, control por rol (`AnimalController`)

## 🗺️ Roadmap backend
- ✅ Fase 1 — Enums y modelos JPA
- ✅ Fase 2 — Autenticación JWT
- ✅ Fase 3 — Granjas y usuarios
- 🚧 Fase 4 — Núcleo reproductivo
  - ✅ `AnimalController` (incremento 1/4)
  - 🔜 `ReproductiveCycleController`, `MatingController`, `LitterController`
- 🔜 Fase 5 — Inventario y sanidad
- 🔜 Fase 6 — Notificaciones, `GlobalExceptionHandler`, `CorsConfig`, `JacksonConfig`

## 🔐 Seguridad
- JWT stateless, sin sesiones en servidor
- Cada endpoint valida el `farm_id` del usuario autenticado contra el recurso solicitado — un recurso de otra granja responde igual que uno inexistente
- Roles: `ADMIN` (CRUD completo) / `WORKER` (lectura + creación, sin editar/eliminar)
- Contraseñas con `BCryptPasswordEncoder`

## 🔧 Base de datos
Triggers en PostgreSQL para lógica crítica independiente del cliente:
- Fecha de parto automática (+114 días desde la monta)
- Alertas de parto, destete y vacunas
- Control de inventario (stock bajo)
- Validación de correspondencia madre/ciclo/camada
- Auditoría de cambios

## 📱 Futuro
- App Android (Kotlin, MVVM + Retrofit)
- App iOS (Swift)
