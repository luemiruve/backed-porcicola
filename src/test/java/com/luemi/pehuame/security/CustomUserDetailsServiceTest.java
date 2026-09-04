package com.luemi.pehuame.security;

import com.luemi.pehuame.enums.UserRole;
import com.luemi.pehuame.model.User;
import com.luemi.pehuame.repository.UserRepository;
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
