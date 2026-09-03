package com.luemi.porcicola.controller;

import com.luemi.porcicola.dto.AnimalDTO;
import com.luemi.porcicola.mapper.AnimalMapper;
import com.luemi.porcicola.repository.AnimalRepository;
import com.luemi.porcicola.repository.FarmRepository;
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

        @Bean
        AnimalRepository animalRepository() {
            return Mockito.mock(AnimalRepository.class);
        }

        @Bean
        FarmRepository farmRepository() {
            return Mockito.mock(FarmRepository.class);
        }

        @Bean
        AnimalMapper animalMapper() {
            return Mockito.mock(AnimalMapper.class);
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
