package com.luemi.pehuame.controller;

import com.luemi.pehuame.dto.MatingDTO;
import com.luemi.pehuame.mapper.MatingMapper;
import com.luemi.pehuame.repository.AnimalRepository;
import com.luemi.pehuame.repository.MatingRepository;
import com.luemi.pehuame.repository.ReproductiveCycleRepository;
import com.luemi.pehuame.service.MatingService;
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
@ContextConfiguration(classes = MatingControllerSecurityTest.Config.class)
class MatingControllerSecurityTest {

    @Configuration
    @EnableMethodSecurity
    @Import(MatingController.class)
    static class Config {
        @Bean
        MatingService matingService() {
            MatingService mock = Mockito.mock(MatingService.class);
            when(mock.create(any(MatingDTO.class))).thenReturn(new MatingDTO());
            when(mock.getById(anyInt())).thenReturn(new MatingDTO());
            when(mock.update(anyInt(), any(MatingDTO.class))).thenReturn(new MatingDTO());
            return mock;
        }

        @Bean
        MatingRepository matingRepository() {
            return Mockito.mock(MatingRepository.class);
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
        MatingMapper matingMapper() {
            return Mockito.mock(MatingMapper.class);
        }
    }

    @Autowired
    private MatingController matingController;

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_canCreateAndRead() {
        assertThatCode(() -> matingController.create(new MatingDTO())).doesNotThrowAnyException();
        assertThatCode(() -> matingController.getById(1)).doesNotThrowAnyException();
        assertThatCode(() -> matingController.list(10)).doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void worker_cannotUpdateOrDelete() {
        assertThatThrownBy(() -> matingController.update(1, new MatingDTO()))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> matingController.delete(1))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canUpdateAndDelete() {
        assertThatCode(() -> matingController.update(1, new MatingDTO())).doesNotThrowAnyException();
        assertThatCode(() -> matingController.delete(1)).doesNotThrowAnyException();
    }
}
