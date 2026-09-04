# 🐷 Porcicola Backend

API REST para gestión integral de una granja porcina. Repositorio de lógica del proyecto **Porcicola** — junto a `porcicola-database` (esquema y Postgres local) y `porcicola-frontend` (cliente móvil), como repos independientes dentro del mismo proyecto.

## 🚀 Stack
- Java 21 + Spring Boot 3.5
- PostgreSQL (Supabase en prod, Docker local en dev — ver el repo `porcicola-database`) · JPA / Hibernate (`ddl-auto=validate` — la BD es la fuente de verdad del esquema)
- JWT stateless (jjwt) + Spring Security (`@PreAuthorize` por rol)

## 🧠 Arquitectura
- REST API en capas: `Controller` → `Service` → `Repository` → `Model`, con `DTO` + `Mapper` por recurso
- Cada request autenticado resuelve un `UserPrincipal` (`farm_id` + rol) vía `CurrentUser.get()`; todo aislamiento multi-tenant se deriva de ahí, nunca de un `farm_id` que venga del cliente
- Respuestas envueltas en `ApiResponse<T>` estándar

## 📦 Funcionalidades implementadas
- **Auth**: registro/login con JWT (`AuthController`)
- **Granjas**: CRUD (`FarmController`)
- **Usuarios**: gestión por granja, roles `ADMIN`/`WORKER` (`UserController`)
- **Animales**: CRUD + filtros por tipo/estado, aislamiento por granja, validación de madre, control por rol (`AnimalController`)

## 🔐 Seguridad
- JWT stateless, sin sesiones en servidor
- Cada endpoint valida el `farm_id` del usuario autenticado contra el recurso solicitado — un recurso de otra granja responde igual que uno inexistente
- Roles: `ADMIN` (CRUD completo) / `WORKER` (lectura + creación, sin editar/eliminar)
- Contraseñas con `BCryptPasswordEncoder`

## ▶️ Correr localmente
Contra Postgres en Docker (repo `porcicola-database`, clonado junto a este):
```bash
cd ../porcicola-database && docker compose up -d
cd ../porcicola-backend && mvn spring-boot:run -Dspring-boot.run.profiles=local
```
Asume `porcicola-database` clonado como carpeta hermana de esta (`../porcicola-database`) — ajusta la ruta si tu checkout local es distinto. El perfil `local` (`src/main/resources/application-local.properties`) trae credenciales de dev fijas, sin depender de `.env`.

Contra Supabase (prod): requiere `.env` en esta carpeta con `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `SSUName`, `SSUPassword` — `mvn spring-boot:run` (sin profile).
