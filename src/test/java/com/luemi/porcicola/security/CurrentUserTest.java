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
