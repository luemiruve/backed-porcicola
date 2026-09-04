package com.luemi.pehuame.service;

import com.luemi.pehuame.dto.FarmDTO;
import com.luemi.pehuame.enums.UserRole;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.model.User;
import com.luemi.pehuame.repository.FarmRepository;
import com.luemi.pehuame.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    @Mock
    private FarmRepository farmRepository;

    @InjectMocks
    private FarmService farmService;

    private Farm farmA;
    private Farm farmB;

    @BeforeEach
    void setUp() {
        farmA = new Farm();
        farmA.setId(1);
        farmA.setName("Farm A");

        farmB = new Farm();
        farmB.setId(2);
        farmB.setName("Farm B");

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
    void getById_returnsFarm_whenOwnFarm() {
        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));

        Farm result = farmService.getById(1);

        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getById_throwsFarmNotFound_whenDifferentFarm() {
        when(farmRepository.findById(2)).thenReturn(Optional.of(farmB));

        assertThatThrownBy(() -> farmService.getById(2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Farm not found");
    }

    @Test
    void getById_throwsFarmNotFound_whenMissing() {
        when(farmRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmService.getById(2))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Farm not found");
    }

    @Test
    void update_appliesChanges_whenOwnFarm() {
        FarmDTO dto = new FarmDTO();
        dto.setName("Updated name");
        dto.setLocation("Updated location");

        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));
        when(farmRepository.save(farmA)).thenReturn(farmA);

        Farm result = farmService.update(1, dto);

        assertThat(result.getName()).isEqualTo("Updated name");
        assertThat(result.getLocation()).isEqualTo("Updated location");
    }

    @Test
    void update_throwsFarmNotFound_whenDifferentFarm() {
        when(farmRepository.findById(2)).thenReturn(Optional.of(farmB));

        assertThatThrownBy(() -> farmService.update(2, new FarmDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Farm not found");
    }
}
