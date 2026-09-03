# AnimalController Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement `AnimalController` (Fase 4, incremento 1) with multi-tenant `farmId` isolation and role-based access control, establishing the security foundation (`UserPrincipal`, `CurrentUser`, `@EnableMethodSecurity`) reused by the next three controllers in Fase 4.

**Architecture:** Standard controller → service → mapper → repository stack, following the existing `User`/`Farm` pattern (field `@Autowired` injection, `ApiResponse<T>` wrapper, plain `RuntimeException` for errors). New: a `UserPrincipal` (`UserDetails` impl carrying `farmId`/`role`) returned by `CustomUserDetailsService`, read via the static `CurrentUser.get()` helper in `AnimalService`, and enforced at the controller via `@PreAuthorize`.

**Tech Stack:** Spring Boot 3.5 / Java 21, Spring Security (`@EnableMethodSecurity`, `@PreAuthorize`), Spring Data JPA, JUnit 5 + Mockito + AssertJ + spring-security-test (all already in `pom.xml`, no new dependencies needed).

**Spec:** `docs/superpowers/specs/2026-08-10-animal-controller-design.md`

## Global Constraints

- Never trust a `farmId` coming from the client (body, path, or query param) — every `AnimalService` method derives it from `CurrentUser.get().getFarmId()`.
- Cross-farm access to an existing `Animal` (or its `mother`) throws the exact same exception as "resource does not exist" (`RuntimeException("Animal not found")` / `RuntimeException("Mother not found")`) — never a 403, so a user from another farm can't distinguish "doesn't exist" from "exists but isn't yours".
- Role enforcement (`WORKER` vs `ADMIN`) is a separate concern from farm isolation and DOES return 403 — implemented purely via `@PreAuthorize` on `AnimalController`, per spec: `POST /animals`, `GET /animals/{id}`, `GET /animals` → `hasAnyRole('ADMIN','WORKER')`; `PUT /animals/{id}`, `DELETE /animals/{id}` → `hasRole('ADMIN')`.
- No `GlobalExceptionHandler` — stay with the project's current style of bare `RuntimeException` + descriptive message (explicitly deferred to Fase 6 per spec).
- `AnimalDTO` has no `farmId` field and is reused as-is for create/update/response (matches `UserDTO`'s pattern, no separate request DTOs).
- **Test environment note:** this sandbox has no `.env`/live Supabase credentials loaded, so `PorcicolaApplicationTests` (`@SpringBootTest`) already fails to boot here for reasons unrelated to this plan (missing `DB_URL`/`SSUName`/`SSUPassword` env vars) — confirmed via `mvn test` before writing this plan. None of the tests in this plan use `@SpringBootTest` or touch a datasource, so they run independently of that pre-existing issue. Run new tests with `mvn test -Dtest=<ClassName>` rather than the full suite, to avoid noise from that unrelated failure.
- `AnimalRepository`'s custom `@Query` method has no dedicated repository-level test (matches the existing project convention — `FarmRepository`/`UserRepository` custom methods are untested too); it's exercised indirectly through `AnimalServiceTest`'s mocked-repository tests.

---

## File Structure

- `src/main/java/com/luemi/porcicola/security/UserPrincipal.java` — new. `UserDetails` implementation carrying `id`, `farmId`, `role` built from a `User` entity.
- `src/main/java/com/luemi/porcicola/security/CurrentUser.java` — new. Static accessor: `CurrentUser.get()` → `UserPrincipal`.
- `src/main/java/com/luemi/porcicola/security/CustomUserDetailsService.java` — modified. Returns `UserPrincipal` instead of Spring's generic `User`.
- `src/main/java/com/luemi/porcicola/security/SecurityConfig.java` — modified. Adds `@EnableMethodSecurity`.
- `src/main/java/com/luemi/porcicola/dto/AnimalDTO.java` — new.
- `src/main/java/com/luemi/porcicola/mapper/AnimalMapper.java` — new.
- `src/main/java/com/luemi/porcicola/repository/AnimalRepository.java` — new.
- `src/main/java/com/luemi/porcicola/service/AnimalService.java` — new.
- `src/main/java/com/luemi/porcicola/controller/AnimalController.java` — new.
- Tests mirror each new/modified class under `src/test/java/...` (see tasks below).

## Task Right-Sizing

Split into 6 tasks: (1) the two brand-new security utility classes, (2) wiring them into the existing security layer, (3) DTO+mapper, (4) repository, (5) service (the biggest, multi-step), (6) controller with both an HTTP-shape test and a role-enforcement test. Each produces an independently testable/reviewable deliverable.

---

### Task 1: `UserPrincipal` + `CurrentUser`

**Files:**
- Create: `src/main/java/com/luemi/porcicola/security/UserPrincipal.java`
- Create: `src/main/java/com/luemi/porcicola/security/CurrentUser.java`
- Test: `src/test/java/com/luemi/porcicola/security/UserPrincipalTest.java`
- Test: `src/test/java/com/luemi/porcicola/security/CurrentUserTest.java`

**Interfaces:**
- Consumes: `com.luemi.porcicola.model.User` (existing: `getId()`, `getEmail()`, `getPasswordHash()`, `getFarm()` → `Farm.getId()`, `getRole()` → `UserRole`).
- Produces: `UserPrincipal(User user)` constructor; `UserPrincipal.getId(): Integer`, `getFarmId(): Integer`, `getRole(): UserRole`, plus standard `UserDetails` methods. `CurrentUser.get(): UserPrincipal`. Both consumed by Task 2 (`CustomUserDetailsService`) and Task 5 (`AnimalService`).

- [ ] **Step 1: Write the failing tests**

`src/test/java/com/luemi/porcicola/security/UserPrincipalTest.java`:
```java
package com.luemi.porcicola.security;

import com.luemi.porcicola.enums.UserRole;
import com.luemi.porcicola.model.Farm;
import com.luemi.porcicola.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    @Test
    void exposesIdFarmIdAndRoleFromUser() {
        Farm farm = new Farm();
        farm.setId(3);

        User user = new User();
        user.setId(7);
        user.setEmail("worker@farm.test");
        user.setPasswordHash("hashed");
        user.setFarm(farm);
        user.setRole(UserRole.WORKER);

        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getId()).isEqualTo(7);
        assertThat(principal.getUsername()).isEqualTo("worker@farm.test");
        assertThat(principal.getPassword()).isEqualTo("hashed");
        assertThat(principal.getFarmId()).isEqualTo(3);
        assertThat(principal.getRole()).isEqualTo(UserRole.WORKER);
    }

    @Test
    void authoritiesArePrefixedWithRole() {
        User user = new User();
        user.setEmail("admin@farm.test");
        user.setPasswordHash("hashed");
        user.setRole(UserRole.ADMIN);

        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void farmIdIsNull_whenUserHasNoFarm() {
        User user = new User();
        user.setEmail("noone@farm.test");
        user.setPasswordHash("hashed");
        user.setRole(UserRole.ADMIN);

        UserPrincipal principal = new UserPrincipal(user);

        assertThat(principal.getFarmId()).isNull();
    }
}
```

`src/test/java/com/luemi/porcicola/security/CurrentUserTest.java`:
```java
package com.luemi.porcicola.security;

import com.luemi.porcicola.enums.UserRole;
import com.luemi.porcicola.model.Farm;
import com.luemi.porcicola.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void get_returnsPrincipalFromSecurityContext() {
        Farm farm = new Farm();
        farm.setId(4);

        User user = new User();
        user.setId(1);
        user.setEmail("admin@farm.test");
        user.setPasswordHash("hashed");
        user.setFarm(farm);
        user.setRole(UserRole.ADMIN);

        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        UserPrincipal result = CurrentUser.get();

        assertThat(result.getFarmId()).isEqualTo(4);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=UserPrincipalTest,CurrentUserTest`
Expected: compilation failure — `UserPrincipal`/`CurrentUser` don't exist yet.

- [ ] **Step 3: Write the implementation**

`src/main/java/com/luemi/porcicola/security/UserPrincipal.java`:
```java
package com.luemi.porcicola.security;

import com.luemi.porcicola.enums.UserRole;
import com.luemi.porcicola.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Integer id;
    private final String email;
    private final String passwordHash;
    private final Integer farmId;
    private final UserRole role;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.farmId = user.getFarm() != null ? user.getFarm().getId() : null;
        this.role = user.getRole();
    }

    public Integer getId() {
        return id;
    }

    public Integer getFarmId() {
        return farmId;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

`src/main/java/com/luemi/porcicola/security/CurrentUser.java`:
```java
package com.luemi.porcicola.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static UserPrincipal get() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -Dtest=UserPrincipalTest,CurrentUserTest`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/luemi/porcicola/security/UserPrincipal.java \
        src/main/java/com/luemi/porcicola/security/CurrentUser.java \
        src/test/java/com/luemi/porcicola/security/UserPrincipalTest.java \
        src/test/java/com/luemi/porcicola/security/CurrentUserTest.java
git commit -m "feat(security): add UserPrincipal and CurrentUser"
```

---

### Task 2: Wire `UserPrincipal` into `CustomUserDetailsService` + enable method security

**Files:**
- Modify: `src/main/java/com/luemi/porcicola/security/CustomUserDetailsService.java`
- Modify: `src/main/java/com/luemi/porcicola/security/SecurityConfig.java`
- Test: `src/test/java/com/luemi/porcicola/security/CustomUserDetailsServiceTest.java`

**Interfaces:**
- Consumes: `UserPrincipal(User)` from Task 1.
- Produces: `CustomUserDetailsService.loadUserByUsername(String): UserDetails` now returns a `UserPrincipal` (so `CurrentUser.get()`'s cast in Task 5 succeeds for real authenticated requests); `SecurityConfig` now supports `@PreAuthorize` (consumed by Task 6).

- [ ] **Step 1: Write the failing test**

`src/test/java/com/luemi/porcicola/security/CustomUserDetailsServiceTest.java`:
```java
package com.luemi.porcicola.security;

import com.luemi.porcicola.enums.UserRole;
import com.luemi.porcicola.model.User;
import com.luemi.porcicola.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_returnsUserPrincipal() {
        User user = new User();
        user.setId(1);
        user.setEmail("admin@farm.test");
        user.setPasswordHash("hashed");
        user.setRole(UserRole.ADMIN);

        when(userRepository.findByEmail("admin@farm.test")).thenReturn(Optional.of(user));

        UserDetails details = customUserDetailsService.loadUserByUsername("admin@farm.test");

        assertThat(details).isInstanceOf(UserPrincipal.class);
        assertThat(((UserPrincipal) details).getId()).isEqualTo(1);
    }

    @Test
    void loadUserByUsername_throws_whenUserMissing() {
        when(userRepository.findByEmail("missing@farm.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@farm.test"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=CustomUserDetailsServiceTest`
Expected: FAIL — `loadUserByUsername` currently returns Spring's built-in `User`, not a `UserPrincipal`, so `isInstanceOf(UserPrincipal.class)` fails.

- [ ] **Step 3: Update `CustomUserDetailsService` and `SecurityConfig`**

`src/main/java/com/luemi/porcicola/security/CustomUserDetailsService.java` (replace the `loadUserByUsername` body):
```java
package com.luemi.porcicola.security;

import com.luemi.porcicola.model.User;
import com.luemi.porcicola.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new UserPrincipal(user);
    }
}
```

`src/main/java/com/luemi/porcicola/security/SecurityConfig.java` (add the import and annotation, rest unchanged):
```java
package com.luemi.porcicola.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=CustomUserDetailsServiceTest`
Expected: PASS (2 tests).

- [ ] **Step 5: Compile the whole project to confirm `SecurityConfig` still builds**

Run: `mvn -o compile`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/luemi/porcicola/security/CustomUserDetailsService.java \
        src/main/java/com/luemi/porcicola/security/SecurityConfig.java \
        src/test/java/com/luemi/porcicola/security/CustomUserDetailsServiceTest.java
git commit -m "feat(security): return UserPrincipal from CustomUserDetailsService, enable method security"
```

---

### Task 3: `AnimalDTO` + `AnimalMapper`

**Files:**
- Create: `src/main/java/com/luemi/porcicola/dto/AnimalDTO.java`
- Create: `src/main/java/com/luemi/porcicola/mapper/AnimalMapper.java`
- Test: `src/test/java/com/luemi/porcicola/mapper/AnimalMapperTest.java`

**Interfaces:**
- Consumes: `com.luemi.porcicola.model.Animal`, `Farm` (existing), `com.luemi.porcicola.enums.{AnimalType,AnimalStatus}` (existing).
- Produces: `AnimalDTO` (fields: `id`, `nfcUid`, `name`, `type: AnimalType`, `birthDate: LocalDate`, `status: AnimalStatus`, `motherId: Integer`, `currentWeight: BigDecimal`, `notes`, plain getters/setters, no `farmId`). `AnimalMapper.toDTO(Animal): AnimalDTO`, `AnimalMapper.toEntity(AnimalDTO, Animal mother, Farm farm): Animal` — consumed by Task 5 (`AnimalService`).

- [ ] **Step 1: Write the failing test**

`src/test/java/com/luemi/porcicola/mapper/AnimalMapperTest.java`:
```java
package com.luemi.porcicola.mapper;

import com.luemi.porcicola.dto.AnimalDTO;
import com.luemi.porcicola.enums.AnimalStatus;
import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Farm;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalMapperTest {

    private final AnimalMapper mapper = new AnimalMapper();

    @Test
    void toDTO_mapsAllFieldsIncludingMotherId() {
        Farm farm = new Farm();
        farm.setId(1);

        Animal mother = new Animal();
        mother.setId(5);

        Animal animal = new Animal();
        animal.setId(10);
        animal.setNfcUid("NFC-1");
        animal.setName("Bella");
        animal.setType(AnimalType.SOW);
        animal.setBirthDate(LocalDate.of(2025, 1, 15));
        animal.setStatus(AnimalStatus.ACTIVE);
        animal.setMother(mother);
        animal.setCurrentWeight(new BigDecimal("120.50"));
        animal.setNotes("Healthy");
        animal.setFarm(farm);

        AnimalDTO dto = mapper.toDTO(animal);

        assertThat(dto.getId()).isEqualTo(10);
        assertThat(dto.getNfcUid()).isEqualTo("NFC-1");
        assertThat(dto.getName()).isEqualTo("Bella");
        assertThat(dto.getType()).isEqualTo(AnimalType.SOW);
        assertThat(dto.getBirthDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(dto.getStatus()).isEqualTo(AnimalStatus.ACTIVE);
        assertThat(dto.getMotherId()).isEqualTo(5);
        assertThat(dto.getCurrentWeight()).isEqualTo(new BigDecimal("120.50"));
        assertThat(dto.getNotes()).isEqualTo("Healthy");
    }

    @Test
    void toDTO_withoutMother_motherIdIsNull() {
        Animal animal = new Animal();
        animal.setId(11);
        animal.setType(AnimalType.PIGLET);

        AnimalDTO dto = mapper.toDTO(animal);

        assertThat(dto.getMotherId()).isNull();
    }

    @Test
    void toEntity_buildsAnimalWithResolvedMotherAndFarm() {
        Farm farm = new Farm();
        farm.setId(2);

        Animal mother = new Animal();
        mother.setId(7);

        AnimalDTO dto = new AnimalDTO();
        dto.setNfcUid("NFC-2");
        dto.setName("Rocky");
        dto.setType(AnimalType.BOAR);
        dto.setBirthDate(LocalDate.of(2024, 6, 1));
        dto.setStatus(AnimalStatus.FATTENING);
        dto.setCurrentWeight(new BigDecimal("80.00"));
        dto.setNotes("None");

        Animal animal = mapper.toEntity(dto, mother, farm);

        assertThat(animal.getNfcUid()).isEqualTo("NFC-2");
        assertThat(animal.getName()).isEqualTo("Rocky");
        assertThat(animal.getType()).isEqualTo(AnimalType.BOAR);
        assertThat(animal.getBirthDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(animal.getStatus()).isEqualTo(AnimalStatus.FATTENING);
        assertThat(animal.getMother()).isSameAs(mother);
        assertThat(animal.getCurrentWeight()).isEqualTo(new BigDecimal("80.00"));
        assertThat(animal.getNotes()).isEqualTo("None");
        assertThat(animal.getFarm()).isSameAs(farm);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=AnimalMapperTest`
Expected: compilation failure — `AnimalDTO`/`AnimalMapper` don't exist yet.

- [ ] **Step 3: Write the implementation**

`src/main/java/com/luemi/porcicola/dto/AnimalDTO.java`:
```java
package com.luemi.porcicola.dto;

import com.luemi.porcicola.enums.AnimalStatus;
import com.luemi.porcicola.enums.AnimalType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AnimalDTO {
    private Integer id;
    private String nfcUid;
    private String name;
    private AnimalType type;
    private LocalDate birthDate;
    private AnimalStatus status;
    private Integer motherId;
    private BigDecimal currentWeight;
    private String notes;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNfcUid() {
        return nfcUid;
    }

    public void setNfcUid(String nfcUid) {
        this.nfcUid = nfcUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AnimalType getType() {
        return type;
    }

    public void setType(AnimalType type) {
        this.type = type;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public AnimalStatus getStatus() {
        return status;
    }

    public void setStatus(AnimalStatus status) {
        this.status = status;
    }

    public Integer getMotherId() {
        return motherId;
    }

    public void setMotherId(Integer motherId) {
        this.motherId = motherId;
    }

    public BigDecimal getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(BigDecimal currentWeight) {
        this.currentWeight = currentWeight;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
```

`src/main/java/com/luemi/porcicola/mapper/AnimalMapper.java`:
```java
package com.luemi.porcicola.mapper;

import com.luemi.porcicola.dto.AnimalDTO;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Farm;
import org.springframework.stereotype.Component;

@Component
public class AnimalMapper {

    public AnimalDTO toDTO(Animal animal) {
        AnimalDTO dto = new AnimalDTO();
        dto.setId(animal.getId());
        dto.setNfcUid(animal.getNfcUid());
        dto.setName(animal.getName());
        dto.setType(animal.getType());
        dto.setBirthDate(animal.getBirthDate());
        dto.setStatus(animal.getStatus());
        dto.setMotherId(animal.getMother() != null ? animal.getMother().getId() : null);
        dto.setCurrentWeight(animal.getCurrentWeight());
        dto.setNotes(animal.getNotes());
        return dto;
    }

    public Animal toEntity(AnimalDTO dto, Animal mother, Farm farm) {
        Animal animal = new Animal();
        animal.setNfcUid(dto.getNfcUid());
        animal.setName(dto.getName());
        animal.setType(dto.getType());
        animal.setBirthDate(dto.getBirthDate());
        animal.setStatus(dto.getStatus());
        animal.setMother(mother);
        animal.setCurrentWeight(dto.getCurrentWeight());
        animal.setNotes(dto.getNotes());
        animal.setFarm(farm);
        return animal;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=AnimalMapperTest`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/luemi/porcicola/dto/AnimalDTO.java \
        src/main/java/com/luemi/porcicola/mapper/AnimalMapper.java \
        src/test/java/com/luemi/porcicola/mapper/AnimalMapperTest.java
git commit -m "feat: add AnimalDTO and AnimalMapper"
```

---

### Task 4: `AnimalRepository`

**Files:**
- Create: `src/main/java/com/luemi/porcicola/repository/AnimalRepository.java`

**Interfaces:**
- Consumes: `com.luemi.porcicola.model.Animal`, `com.luemi.porcicola.enums.{AnimalType,AnimalStatus}`.
- Produces: `AnimalRepository extends JpaRepository<Animal, Integer>` with `findByFarmIdAndFilters(Integer farmId, AnimalType type, AnimalStatus status): List<Animal>` — consumed by Task 5.

No dedicated test for this task (see Global Constraints — matches existing project convention for custom `@Query` repository methods; exercised indirectly by `AnimalServiceTest`'s mocked-repository tests in Task 5).

- [ ] **Step 1: Write the implementation**

`src/main/java/com/luemi/porcicola/repository/AnimalRepository.java`:
```java
package com.luemi.porcicola.repository;

import com.luemi.porcicola.enums.AnimalStatus;
import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.model.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Integer> {

    @Query("SELECT a FROM Animal a WHERE a.farm.id = :farmId " +
           "AND (:type IS NULL OR a.type = :type) " +
           "AND (:status IS NULL OR a.status = :status)")
    List<Animal> findByFarmIdAndFilters(Integer farmId, AnimalType type, AnimalStatus status);
}
```

- [ ] **Step 2: Compile to confirm it builds**

Run: `mvn -o compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/luemi/porcicola/repository/AnimalRepository.java
git commit -m "feat: add AnimalRepository with farm/type/status filter query"
```

---

### Task 5: `AnimalService`

**Files:**
- Create: `src/main/java/com/luemi/porcicola/service/AnimalService.java`
- Test: `src/test/java/com/luemi/porcicola/service/AnimalServiceTest.java`

**Interfaces:**
- Consumes: `AnimalRepository` (Task 4), `FarmRepository` (existing), `AnimalMapper` (Task 3), `CurrentUser.get()` → `UserPrincipal.getFarmId()` (Task 1/2).
- Produces: `AnimalService.create(AnimalDTO): AnimalDTO`, `getById(Integer): AnimalDTO`, `list(AnimalType, AnimalStatus): List<AnimalDTO>`, `update(Integer, AnimalDTO): AnimalDTO`, `delete(Integer): void` — all consumed by Task 6 (`AnimalController`).

- [ ] **Step 1: Write the failing tests**

`src/test/java/com/luemi/porcicola/service/AnimalServiceTest.java`:
```java
package com.luemi.porcicola.service;

import com.luemi.porcicola.dto.AnimalDTO;
import com.luemi.porcicola.enums.AnimalStatus;
import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.enums.UserRole;
import com.luemi.porcicola.mapper.AnimalMapper;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Farm;
import com.luemi.porcicola.model.User;
import com.luemi.porcicola.repository.AnimalRepository;
import com.luemi.porcicola.repository.FarmRepository;
import com.luemi.porcicola.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private FarmRepository farmRepository;

    @Spy
    private AnimalMapper animalMapper = new AnimalMapper();

    @InjectMocks
    private AnimalService animalService;

    private Farm farmA;
    private Farm farmB;

    @BeforeEach
    void setUp() {
        farmA = new Farm();
        farmA.setId(1);

        farmB = new Farm();
        farmB.setId(2);

        authenticateAsFarm(farmA, UserRole.ADMIN);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsFarm(Farm farm, UserRole role) {
        User user = new User();
        user.setId(99);
        user.setEmail("owner@farm.test");
        user.setPasswordHash("hash");
        user.setFarm(farm);
        user.setRole(role);

        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @Test
    void create_assignsCurrentUserFarm() {
        AnimalDTO dto = new AnimalDTO();
        dto.setType(AnimalType.SOW);
        dto.setName("Bella");

        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> {
            Animal saved = invocation.getArgument(0);
            saved.setId(100);
            return saved;
        });

        AnimalDTO result = animalService.create(dto);

        assertThat(result.getId()).isEqualTo(100);
        assertThat(result.getName()).isEqualTo("Bella");
    }

    @Test
    void create_withMotherFromAnotherFarm_throws() {
        AnimalDTO dto = new AnimalDTO();
        dto.setType(AnimalType.PIGLET);
        dto.setMotherId(5);

        Animal motherInOtherFarm = new Animal();
        motherInOtherFarm.setId(5);
        motherInOtherFarm.setFarm(farmB);

        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));
        when(animalRepository.findById(5)).thenReturn(Optional.of(motherInOtherFarm));

        assertThatThrownBy(() -> animalService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mother not found");
    }

    @Test
    void getById_returnsAnimal_whenSameFarm() {
        Animal animal = new Animal();
        animal.setId(10);
        animal.setType(AnimalType.SOW);
        animal.setFarm(farmA);

        when(animalRepository.findById(10)).thenReturn(Optional.of(animal));

        AnimalDTO dto = animalService.getById(10);

        assertThat(dto.getId()).isEqualTo(10);
    }

    @Test
    void getById_throwsAnimalNotFound_whenDifferentFarm() {
        Animal animal = new Animal();
        animal.setId(10);
        animal.setType(AnimalType.SOW);
        animal.setFarm(farmB);

        when(animalRepository.findById(10)).thenReturn(Optional.of(animal));

        assertThatThrownBy(() -> animalService.getById(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Animal not found");
    }

    @Test
    void getById_throwsAnimalNotFound_whenMissing() {
        when(animalRepository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.getById(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Animal not found");
    }

    @Test
    void list_filtersByCurrentUserFarmTypeAndStatus() {
        Animal animal = new Animal();
        animal.setId(1);
        animal.setType(AnimalType.SOW);
        animal.setFarm(farmA);

        when(animalRepository.findByFarmIdAndFilters(1, AnimalType.SOW, AnimalStatus.ACTIVE))
                .thenReturn(List.of(animal));

        List<AnimalDTO> result = animalService.list(AnimalType.SOW, AnimalStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        verify(animalRepository).findByFarmIdAndFilters(1, AnimalType.SOW, AnimalStatus.ACTIVE);
    }

    @Test
    void update_appliesChanges_whenSameFarm() {
        Animal existing = new Animal();
        existing.setId(10);
        existing.setType(AnimalType.SOW);
        existing.setName("Old name");
        existing.setFarm(farmA);

        AnimalDTO dto = new AnimalDTO();
        dto.setType(AnimalType.SOW);
        dto.setName("New name");

        when(animalRepository.findById(10)).thenReturn(Optional.of(existing));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnimalDTO result = animalService.update(10, dto);

        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getName()).isEqualTo("New name");
    }

    @Test
    void update_throwsAnimalNotFound_whenDifferentFarm() {
        Animal existing = new Animal();
        existing.setId(10);
        existing.setType(AnimalType.SOW);
        existing.setFarm(farmB);

        when(animalRepository.findById(10)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> animalService.update(10, new AnimalDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Animal not found");
    }

    @Test
    void delete_removesAnimal_whenSameFarm() {
        Animal existing = new Animal();
        existing.setId(10);
        existing.setType(AnimalType.SOW);
        existing.setFarm(farmA);

        when(animalRepository.findById(10)).thenReturn(Optional.of(existing));

        animalService.delete(10);

        verify(animalRepository).delete(existing);
    }

    @Test
    void delete_throwsAnimalNotFound_whenDifferentFarm() {
        Animal existing = new Animal();
        existing.setId(10);
        existing.setType(AnimalType.SOW);
        existing.setFarm(farmB);

        when(animalRepository.findById(10)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> animalService.delete(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Animal not found");
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=AnimalServiceTest`
Expected: compilation failure — `AnimalService` doesn't exist yet.

- [ ] **Step 3: Write the implementation**

`src/main/java/com/luemi/porcicola/service/AnimalService.java`:
```java
package com.luemi.porcicola.service;

import com.luemi.porcicola.dto.AnimalDTO;
import com.luemi.porcicola.enums.AnimalStatus;
import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.mapper.AnimalMapper;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Farm;
import com.luemi.porcicola.repository.AnimalRepository;
import com.luemi.porcicola.repository.FarmRepository;
import com.luemi.porcicola.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnimalService {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private AnimalMapper animalMapper;

    @Transactional
    public AnimalDTO create(AnimalDTO dto) {
        Integer farmId = CurrentUser.get().getFarmId();
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));
        Animal mother = resolveMother(dto.getMotherId(), farmId);

        Animal animal = animalMapper.toEntity(dto, mother, farm);

        return animalMapper.toDTO(animalRepository.save(animal));
    }

    public AnimalDTO getById(Integer id) {
        return animalMapper.toDTO(getOwnedAnimal(id));
    }

    public List<AnimalDTO> list(AnimalType type, AnimalStatus status) {
        Integer farmId = CurrentUser.get().getFarmId();
        return animalRepository.findByFarmIdAndFilters(farmId, type, status)
                .stream()
                .map(animalMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnimalDTO update(Integer id, AnimalDTO dto) {
        Animal existing = getOwnedAnimal(id);
        Animal mother = resolveMother(dto.getMotherId(), existing.getFarm().getId());

        Animal animal = animalMapper.toEntity(dto, mother, existing.getFarm());
        animal.setId(existing.getId());

        return animalMapper.toDTO(animalRepository.save(animal));
    }

    @Transactional
    public void delete(Integer id) {
        Animal animal = getOwnedAnimal(id);
        animalRepository.delete(animal);
    }

    private Animal getOwnedAnimal(Integer id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal not found"));
        if (!animal.getFarm().getId().equals(CurrentUser.get().getFarmId())) {
            throw new RuntimeException("Animal not found");
        }
        return animal;
    }

    private Animal resolveMother(Integer motherId, Integer farmId) {
        if (motherId == null) {
            return null;
        }
        Animal mother = animalRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found"));
        if (!mother.getFarm().getId().equals(farmId)) {
            throw new RuntimeException("Mother not found");
        }
        return mother;
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -Dtest=AnimalServiceTest`
Expected: PASS (10 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/luemi/porcicola/service/AnimalService.java \
        src/test/java/com/luemi/porcicola/service/AnimalServiceTest.java
git commit -m "feat: add AnimalService with farm isolation and mother validation"
```

---

### Task 6: `AnimalController`

**Files:**
- Create: `src/main/java/com/luemi/porcicola/controller/AnimalController.java`
- Test: `src/test/java/com/luemi/porcicola/controller/AnimalControllerTest.java` (HTTP shape via standalone MockMvc, no security — mirrors how a request/response actually looks)
- Test: `src/test/java/com/luemi/porcicola/controller/AnimalControllerSecurityTest.java` (role enforcement via `@PreAuthorize`, calling controller methods through a minimal `@EnableMethodSecurity` context — bypasses HTTP/JWT entirely to isolate the authorization decision itself; scope note below)

**Interfaces:**
- Consumes: `AnimalService` (Task 5), `AnimalDTO` (Task 3).
- Produces: REST endpoints `POST /animals`, `GET /animals/{id}`, `GET /animals`, `PUT /animals/{id}`, `DELETE /animals/{id}` — this is the final component of the increment, nothing downstream consumes it in this plan.

**Scope note on `AnimalControllerSecurityTest`:** it verifies that `@PreAuthorize` throws `org.springframework.security.access.AccessDeniedException` for a disallowed role by invoking the controller bean directly (through its method-security AOP proxy) — it does not go through `MockMvc`/HTTP, so it does not verify that Spring's servlet layer eventually turns that exception into an HTTP 403 (that translation is standard Spring Security `ExceptionTranslationFilter` behavior, already relied upon implicitly wherever `@PreAuthorize` is used elsewhere once this ships) . This keeps the test fast and independent of the datasource/env-var issue noted in Global Constraints.

- [ ] **Step 1: Write the failing tests**

`src/test/java/com/luemi/porcicola/controller/AnimalControllerTest.java`:
```java
package com.luemi.porcicola.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luemi.porcicola.dto.AnimalDTO;
import com.luemi.porcicola.enums.AnimalStatus;
import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.service.AnimalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnimalControllerTest {

    @Mock
    private AnimalService animalService;

    @InjectMocks
    private AnimalController animalController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(animalController).build();
    }

    @Test
    void create_returns201WithCreatedAnimal() throws Exception {
        AnimalDTO request = new AnimalDTO();
        request.setName("Bella");
        request.setType(AnimalType.SOW);

        AnimalDTO created = new AnimalDTO();
        created.setId(1);
        created.setName("Bella");
        created.setType(AnimalType.SOW);

        when(animalService.create(any(AnimalDTO.class))).thenReturn(created);

        mockMvc.perform(post("/animals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Bella"));
    }

    @Test
    void getById_returns200WithAnimal() throws Exception {
        AnimalDTO animal = new AnimalDTO();
        animal.setId(1);
        animal.setName("Bella");

        when(animalService.getById(1)).thenReturn(animal);

        mockMvc.perform(get("/animals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void list_passesTypeAndStatusFilters() throws Exception {
        when(animalService.list(AnimalType.SOW, AnimalStatus.ACTIVE)).thenReturn(List.of());

        mockMvc.perform(get("/animals").param("type", "SOW").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void update_returns200WithUpdatedAnimal() throws Exception {
        AnimalDTO request = new AnimalDTO();
        request.setName("New name");
        request.setType(AnimalType.SOW);

        AnimalDTO updated = new AnimalDTO();
        updated.setId(1);
        updated.setName("New name");

        when(animalService.update(eq(1), any(AnimalDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/animals/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New name"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/animals/1"))
                .andExpect(status().isNoContent());
    }
}
```

`src/test/java/com/luemi/porcicola/controller/AnimalControllerSecurityTest.java`:
```java
package com.luemi.porcicola.controller;

import com.luemi.porcicola.dto.AnimalDTO;
import com.luemi.porcicola.service.AnimalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AnimalControllerSecurityTest.Config.class)
class AnimalControllerSecurityTest {

    @Configuration
    @EnableMethodSecurity
    @Import(AnimalController.class)
    static class Config {
        @Bean
        AnimalService animalService() {
            AnimalService mock = Mockito.mock(AnimalService.class);
            when(mock.create(any(AnimalDTO.class))).thenReturn(new AnimalDTO());
            when(mock.getById(anyInt())).thenReturn(new AnimalDTO());
            when(mock.update(anyInt(), any(AnimalDTO.class))).thenReturn(new AnimalDTO());
            return mock;
        }
    }

    @Autowired
    private AnimalController animalController;

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_canCreateAndRead() {
        assertThatCode(() -> animalController.create(new AnimalDTO())).doesNotThrowAnyException();
        assertThatCode(() -> animalController.getById(1)).doesNotThrowAnyException();
        assertThatCode(() -> animalController.list(null, null)).doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_cannotUpdateOrDelete() {
        assertThatThrownBy(() -> animalController.update(1, new AnimalDTO()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> animalController.delete(1))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canUpdateAndDelete() {
        assertThatCode(() -> animalController.update(1, new AnimalDTO())).doesNotThrowAnyException();
        assertThatCode(() -> animalController.delete(1)).doesNotThrowAnyException();
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=AnimalControllerTest,AnimalControllerSecurityTest`
Expected: compilation failure — `AnimalController` doesn't exist yet.

- [ ] **Step 3: Write the implementation**

`src/main/java/com/luemi/porcicola/controller/AnimalController.java`:
```java
package com.luemi.porcicola.controller;

import com.luemi.porcicola.dto.AnimalDTO;
import com.luemi.porcicola.dto.ApiResponse;
import com.luemi.porcicola.enums.AnimalStatus;
import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.service.AnimalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/animals")
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @PostMapping
    public ResponseEntity<ApiResponse<AnimalDTO>> create(@RequestBody AnimalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(animalService.create(dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnimalDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(animalService.getById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AnimalDTO>>> list(
            @RequestParam(required = false) AnimalType type,
            @RequestParam(required = false) AnimalStatus status) {
        return ResponseEntity.ok(new ApiResponse<>(animalService.list(type, status)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AnimalDTO>> update(
            @PathVariable Integer id,
            @RequestBody AnimalDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(animalService.update(id, dto)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        animalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -Dtest=AnimalControllerTest,AnimalControllerSecurityTest`
Expected: PASS (8 tests).

- [ ] **Step 5: Full project compile + full new-test-suite run**

Run: `mvn -o compile`
Expected: BUILD SUCCESS.

Run: `mvn test -Dtest=UserPrincipalTest,CurrentUserTest,CustomUserDetailsServiceTest,AnimalMapperTest,AnimalServiceTest,AnimalControllerTest,AnimalControllerSecurityTest`
Expected: PASS (27 tests total).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/luemi/porcicola/controller/AnimalController.java \
        src/test/java/com/luemi/porcicola/controller/AnimalControllerTest.java \
        src/test/java/com/luemi/porcicola/controller/AnimalControllerSecurityTest.java
git commit -m "feat: add AnimalController with role-based access control"
```
