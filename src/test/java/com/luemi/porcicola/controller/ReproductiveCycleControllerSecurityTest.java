package com.luemi.porcicola.controller;

import com.luemi.porcicola.dto.ReproductiveCycleDTO;
import com.luemi.porcicola.mapper.ReproductiveCycleMapper;
import com.luemi.porcicola.repository.AnimalRepository;
import com.luemi.porcicola.repository.FarmRepository;
import com.luemi.porcicola.repository.ReproductiveCycleRepository;
import com.luemi.porcicola.service.ReproductiveCycleService;
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
@ContextConfiguration(classes = ReproductiveCycleControllerSecurityTest.Config.class)
class ReproductiveCycleControllerSecurityTest {

    @Configuration
    @EnableMethodSecurity
    @Import(ReproductiveCycleController.class)
    static class Config {
        @Bean
        ReproductiveCycleService reproductiveCycleService() {
            ReproductiveCycleService mock = Mockito.mock(ReproductiveCycleService.class);
            when(mock.create(any(ReproductiveCycleDTO.class))).thenReturn(new ReproductiveCycleDTO());
            when(mock.getById(anyInt())).thenReturn(new ReproductiveCycleDTO());
            when(mock.update(anyInt(), any(ReproductiveCycleDTO.class))).thenReturn(new ReproductiveCycleDTO());
            return mock;
        }

        @Bean
        ReproductiveCycleRepository reproductiveCycleRepository() {
            return Mockito.mock(ReproductiveCycleRepository.class);
        }

        @Bean
        AnimalRepository animalRepository() {
            return Mockito.mock(AnimalRepository.class);
        }

        @Bean
        FarmRepository farmRepository() {
            return Mockito.mock(FarmRepository.class);
        }

        @Bean
        ReproductiveCycleMapper reproductiveCycleMapper() {
            return Mockito.mock(ReproductiveCycleMapper.class);
        }
    }

    @Autowired
    private ReproductiveCycleController reproductiveCycleController;

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_canCreateAndRead() {
        assertThatCode(() -> reproductiveCycleController.create(new ReproductiveCycleDTO())).doesNotThrowAnyException();
        assertThatCode(() -> reproductiveCycleController.getById(1)).doesNotThrowAnyException();
        assertThatCode(() -> reproductiveCycleController.list(null, null)).doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_cannotUpdateOrDelete() {
        assertThatThrownBy(() -> reproductiveCycleController.update(1, new ReproductiveCycleDTO()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> reproductiveCycleController.delete(1))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canUpdateAndDelete() {
        assertThatCode(() -> reproductiveCycleController.update(1, new ReproductiveCycleDTO())).doesNotThrowAnyException();
        assertThatCode(() -> reproductiveCycleController.delete(1)).doesNotThrowAnyException();
    }
}
