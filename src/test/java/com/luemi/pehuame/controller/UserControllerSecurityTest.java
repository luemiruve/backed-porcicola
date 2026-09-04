package com.luemi.pehuame.controller;

import com.luemi.pehuame.dto.ChangePasswordRequest;
import com.luemi.pehuame.dto.RegisterWorkerRequest;
import com.luemi.pehuame.dto.UserDTO;
import com.luemi.pehuame.mapper.UserMapper;
import com.luemi.pehuame.repository.FarmRepository;
import com.luemi.pehuame.repository.UserRepository;
import com.luemi.pehuame.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserControllerSecurityTest.Config.class)
class UserControllerSecurityTest {

    @Configuration
    @EnableMethodSecurity
    @Import(UserController.class)
    static class Config {
        @Bean
        UserService userService() {
            UserService mock = Mockito.mock(UserService.class);
            when(mock.listByFarm(anyInt())).thenReturn(java.util.List.of());
            when(mock.getById(anyInt())).thenReturn(new UserDTO());
            when(mock.createWorker(anyInt(), any(RegisterWorkerRequest.class))).thenReturn(new UserDTO());
            when(mock.update(anyInt(), any(UserDTO.class))).thenReturn(new UserDTO());
            return mock;
        }

        @Bean
        UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        FarmRepository farmRepository() {
            return Mockito.mock(FarmRepository.class);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return Mockito.mock(PasswordEncoder.class);
        }

        @Bean
        UserMapper userMapper() {
            return Mockito.mock(UserMapper.class);
        }
    }

    @Autowired
    private UserController userController;

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_cannotListFarmUsers() {
        assertThatThrownBy(() -> userController.listByFarm(1))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canListFarmUsers() {
        assertThatCode(() -> userController.listByFarm(1)).doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_canReadUserById() {
        assertThatCode(() -> userController.getById(1)).doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_cannotCreateWorker() {
        assertThatThrownBy(() -> userController.createWorker(1, new RegisterWorkerRequest()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canCreateWorker() {
        assertThatCode(() -> userController.createWorker(1, new RegisterWorkerRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_canCallUpdate() {
        assertThatCode(() -> userController.update(1, new UserDTO())).doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_canCallChangePassword() {
        assertThatCode(() -> userController.changePassword(1, new ChangePasswordRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_cannotDeactivate() {
        assertThatThrownBy(() -> userController.deactivate(1))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canDeactivate() {
        assertThatCode(() -> userController.deactivate(1)).doesNotThrowAnyException();
    }
}
