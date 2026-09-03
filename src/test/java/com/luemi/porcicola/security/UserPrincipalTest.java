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
