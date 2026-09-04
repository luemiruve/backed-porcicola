package com.luemi.pehuame.controller;

import com.luemi.pehuame.dto.FarmDTO;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.repository.FarmRepository;
import com.luemi.pehuame.service.FarmService;
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
@ContextConfiguration(classes = FarmControllerSecurityTest.Config.class)
class FarmControllerSecurityTest {

    @Configuration
    @EnableMethodSecurity
    @Import(FarmController.class)
    static class Config {
        @Bean
        FarmService farmService() {
            FarmService mock = Mockito.mock(FarmService.class);
            when(mock.getById(anyInt())).thenReturn(new Farm());
            when(mock.update(anyInt(), any(FarmDTO.class))).thenReturn(new Farm());
            return mock;
        }

        @Bean
        FarmRepository farmRepository() {
            return Mockito.mock(FarmRepository.class);
        }
    }

    @Autowired
    private FarmController farmController;

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_canRead() {
        assertThatCode(() -> farmController.getById(1)).doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_cannotUpdate() {
        assertThatThrownBy(() -> farmController.update(1, new FarmDTO()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canUpdate() {
        assertThatCode(() -> farmController.update(1, new FarmDTO())).doesNotThrowAnyException();
    }
}
