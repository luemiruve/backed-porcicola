# Fase 4 (incremento 1): AnimalController

**Fecha:** 2026-08-10
**Estado del proyecto:** Fase 4 — Núcleo Reproductivo (retomada hoy, ver página de Notion "🐖 Porcicola")

## Contexto

El backend tiene stack completo (controller/service/repository/dto) solo para `Farm` y `User` (+ auth). Las 8 entidades restantes del dominio (`Animal`, `ReproductiveCycle`, `Mating`, `Litter`, `AnimalHealthEvent`, `LitterHealthEvent`, `Supply`, `Notification`) solo tienen el modelo JPA — la Fase 4 del roadmap (Núcleo Reproductivo) cubre las primeras cuatro.

Este spec cubre el primer incremento de la Fase 4: `AnimalController`. Los otros tres controllers de la fase (`ReproductiveCycle`, `Mating`, `Litter`) se diseñarán en incrementos separados, cada uno construido y probado antes de pasar al siguiente.

### Hallazgos que motivan este diseño

Al revisar la capa de seguridad existente se encontraron dos huecos respecto a lo documentado:

1. **Sin aislamiento por `farm_id`**: el JWT incluye el claim `farmId`, pero ningún controller (`FarmController`, `UserController`) lo compara contra el recurso solicitado. `CustomUserDetailsService` construye un `UserDetails` genérico de Spring que no expone `farmId` a los controllers/services.
2. **Sin control por rol**: `SecurityConfig` solo exige `authenticated()`. No hay ninguna regla `ADMIN` vs `WORKER`, pese a que la documentación del proyecto lo describe como implementado.

`AnimalController` será el primer endpoint en corregir ambos, sentando una base reutilizable para las Fases 4-6.

## Arquitectura

### Autenticación: `UserPrincipal` + `CurrentUser`

- `security/UserPrincipal.java` — implementa `UserDetails`. Campos: `id`, `email`, `farmId`, `role` (`UserRole`). Las autoridades (`GrantedAuthority`) se derivan de `role` (`ROLE_ADMIN` / `ROLE_WORKER`) para que funcione con `@PreAuthorize("hasRole(...)")`.
- `CustomUserDetailsService.loadUserByUsername` se modifica para devolver `UserPrincipal` en vez del `User` genérico de Spring Security, poblado desde el `User` (BD) recién cargado — el `farmId` usado en cada request es siempre el actual en BD, no el del claim JWT (que puede quedar desactualizado si el usuario cambia de granja antes de que expire el token).
- `security/CurrentUser.java` — utilidad estática sin estado: `CurrentUser.get()` lee `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` y hace cast a `UserPrincipal`. Evita repetir ese boilerplate en cada controller/service futuro.
- `SecurityConfig` agrega `@EnableMethodSecurity` para habilitar `@PreAuthorize`.

### Aislamiento multi-tenant

Todo método de `AnimalService` recibe (implícitamente, vía `CurrentUser.get().getFarmId()`) el `farmId` del usuario autenticado y lo usa como filtro obligatorio en cada query. Nunca se confía en un `farmId` que venga del cliente (ni por body ni por path/query param).

- `list`: siempre filtrado por `farmId` del principal, más `type`/`status` opcionales.
- `getById`/`update`/`delete`: se carga el `Animal`, se compara `animal.getFarm().getId()` contra `CurrentUser.get().getFarmId()`. Si no coincide, se lanza la misma excepción que "no encontrado" (ver Manejo de errores) — nunca un 403, para no confirmarle a un usuario de otra granja que el recurso existe.
- `create`: el `Farm` de la entidad se asigna siempre desde `CurrentUser.get().getFarmId()`, ignorando cualquier `farmId` que llegue en el DTO de entrada (el DTO de entrada no tiene ese campo, ver DTO).

### Control por rol

- `POST /animals`, `GET /animals/{id}`, `GET /animals` → `@PreAuthorize("hasAnyRole('ADMIN','WORKER')")`
- `PUT /animals/{id}`, `DELETE /animals/{id}` → `@PreAuthorize("hasRole('ADMIN')")`

## Componentes

### `dto/AnimalDTO.java`
Campos: `id`, `nfcUid`, `name`, `type` (`AnimalType`), `birthDate`, `status` (`AnimalStatus`), `motherId` (`Integer`, nullable — id del `Animal` madre), `currentWeight` (`BigDecimal`), `notes`.
Sin `farmId` (se asigna server-side, ver arriba). Se reutiliza el mismo DTO para create, update y response — igual patrón que `UserDTO`, sin crear `AnimalCreateRequest`/`AnimalUpdateRequest` separados porque no hay campos exclusivos de creación (a diferencia de `User`, que necesita password solo al crear).

### `mapper/AnimalMapper.java`
- `toDTO(Animal)`: mapea campos planos + `motherId = animal.getMother() != null ? animal.getMother().getId() : null`.
- `toEntity(AnimalDTO, Animal mother, Farm farm)`: construye/actualiza la entidad; recibe `mother` y `farm` ya resueltos por el service (el mapper no toca repositorios).

Sigue el patrón de `UserMapper` (clase dedicada) en vez del mapeo manual dentro del service que usa hoy `FarmService` — es el patrón que escalará mejor con 3 controllers más por venir en esta fase.

### `repository/AnimalRepository.java`
`extends JpaRepository<Animal, Integer>` más:
```java
@Query("SELECT a FROM Animal a WHERE a.farm.id = :farmId " +
       "AND (:type IS NULL OR a.type = :type) " +
       "AND (:status IS NULL OR a.status = :status)")
List<Animal> findByFarmIdAndFilters(Integer farmId, AnimalType type, AnimalStatus status);
```
Mismo estilo simple de query ya usado en el proyecto (parámetros nullable en `@Query`), sin introducir Specifications para solo dos filtros opcionales.

### `service/AnimalService.java`
Métodos: `create(AnimalDTO)`, `getById(Integer id)`, `list(AnimalType type, AnimalStatus status)`, `update(Integer id, AnimalDTO)`, `delete(Integer id)`.
Todos resuelven `farmId` vía `CurrentUser.get()` y aplican las reglas de aislamiento descritas arriba. `create`/`update` resuelven `motherId` → `Animal` vía `AnimalRepository.findById`, validando que la madre pertenezca a la misma granja (si no, `RuntimeException("Mother not found")`).

### `controller/AnimalController.java`
- `POST /animals` → `201 Created` + `ApiResponse<AnimalDTO>`
- `GET /animals/{id}` → `200 OK` + `ApiResponse<AnimalDTO>`
- `GET /animals?type=&status=` → `200 OK` + `ApiResponse<List<AnimalDTO>>`
- `PUT /animals/{id}` → `200 OK` + `ApiResponse<AnimalDTO>`
- `DELETE /animals/{id}` → `204 No Content`

Mismo wrapper `ApiResponse<T>` ya estándar en el proyecto.

## Manejo de errores

Se mantiene el estilo actual: `RuntimeException` con mensaje descriptivo, sin `GlobalExceptionHandler` (eso es explícitamente alcance de Fase 6, no se adelanta aquí). Casos:
- Animal no existe, o existe pero es de otra granja → `RuntimeException("Animal not found")`.
- `motherId` no existe, o es de otra granja → `RuntimeException("Mother not found")`.

## Testing

Primer test real del proyecto (hasta ahora solo existe el test de contexto autogenerado). `spring-security-test` ya está en `pom.xml`.

`AnimalControllerTest` (o `AnimalServiceTest`, a decidir en el plan de implementación) cubre como mínimo:
1. Crear un animal y recuperarlo por id.
2. Un usuario de la granja A no puede leer/actualizar/eliminar un animal de la granja B (404, no 403).
3. `WORKER` puede crear y leer, pero recibe 403 en `PUT`/`DELETE`.
4. Filtros de `list` por `type` y `status` combinados.

## Fuera de alcance (explícitamente diferido)

- `ReproductiveCycleController`, `MatingController`, `LitterController` — incrementos separados de esta misma Fase 4.
- `GlobalExceptionHandler`, `CorsConfig`, `JacksonConfig` — Fase 6.
- RBAC en `FarmController`/`UserController` existentes — no se toca en este incremento, aunque ahora ya existe la base (`UserPrincipal`, `@EnableMethodSecurity`) para hacerlo después.
