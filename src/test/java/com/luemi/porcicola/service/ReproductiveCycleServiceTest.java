package com.luemi.porcicola.service;

import com.luemi.porcicola.dto.ReproductiveCycleDTO;
import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.enums.CycleStatus;
import com.luemi.porcicola.enums.UserRole;
import com.luemi.porcicola.mapper.ReproductiveCycleMapper;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Farm;
import com.luemi.porcicola.model.ReproductiveCycle;
import com.luemi.porcicola.model.User;
import com.luemi.porcicola.repository.AnimalRepository;
import com.luemi.porcicola.repository.FarmRepository;
import com.luemi.porcicola.repository.ReproductiveCycleRepository;
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
class ReproductiveCycleServiceTest {

    @Mock
    private ReproductiveCycleRepository reproductiveCycleRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private FarmRepository farmRepository;

    @Spy
    private ReproductiveCycleMapper reproductiveCycleMapper = new ReproductiveCycleMapper();

    @InjectMocks
    private ReproductiveCycleService reproductiveCycleService;

    private Farm farmA;
    private Farm farmB;

    @BeforeEach
    void setUp() {
        farmA = new Farm();
        farmA.setId(1);

        farmB = new Farm();
        farmB.setId(2);

        authenticateAsFarm(farmA);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsFarm(Farm farm) {
        User user = new User();
        user.setId(99);
        user.setEmail("owner@farm.test");
        user.setPasswordHash("hash");
        user.setFarm(farm);
        user.setRole(UserRole.ADMIN);

        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private Animal sow(Integer id, Farm farm) {
        Animal sow = new Animal();
        sow.setId(id);
        sow.setType(AnimalType.SOW);
        sow.setFarm(farm);
        return sow;
    }

    @Test
    void create_assignsCurrentUserFarmAndComputesFarrowingNumber() {
        Animal sow = sow(5, farmA);
        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();
        dto.setSowId(5);

        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));
        when(animalRepository.findById(5)).thenReturn(Optional.of(sow));
        when(reproductiveCycleRepository.findBySowIdAndStatusIn(5, List.of(CycleStatus.GESTATION, CycleStatus.LACTATION)))
                .thenReturn(List.of());
        when(reproductiveCycleRepository.countBySowId(5)).thenReturn(1L);
        when(reproductiveCycleRepository.save(any(ReproductiveCycle.class))).thenAnswer(invocation -> {
            ReproductiveCycle saved = invocation.getArgument(0);
            saved.setId(100);
            return saved;
        });

        ReproductiveCycleDTO result = reproductiveCycleService.create(dto);

        assertThat(result.getId()).isEqualTo(100);
        assertThat(result.getSowId()).isEqualTo(5);
        assertThat(result.getFarrowingNumber()).isEqualTo(2);
    }

    @Test
    void create_withSowFromAnotherFarm_throws() {
        Animal sowInOtherFarm = sow(5, farmB);
        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();
        dto.setSowId(5);

        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));
        when(animalRepository.findById(5)).thenReturn(Optional.of(sowInOtherFarm));

        assertThatThrownBy(() -> reproductiveCycleService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sow not found");
    }

    @Test
    void create_withNonexistentSow_throws() {
        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();
        dto.setSowId(999);

        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));
        when(animalRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reproductiveCycleService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sow not found");
    }

    @Test
    void create_withNonSowAnimal_throws() {
        Animal notASow = new Animal();
        notASow.setId(5);
        notASow.setType(AnimalType.BOAR);
        notASow.setFarm(farmA);
        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();
        dto.setSowId(5);

        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));
        when(animalRepository.findById(5)).thenReturn(Optional.of(notASow));

        assertThatThrownBy(() -> reproductiveCycleService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sow not found");
    }

    @Test
    void create_withSowAlreadyHavingActiveCycle_throws() {
        Animal sow = sow(5, farmA);
        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();
        dto.setSowId(5);

        ReproductiveCycle existingActive = new ReproductiveCycle();
        existingActive.setId(50);
        existingActive.setStatus(CycleStatus.GESTATION);

        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));
        when(animalRepository.findById(5)).thenReturn(Optional.of(sow));
        when(reproductiveCycleRepository.findBySowIdAndStatusIn(5, List.of(CycleStatus.GESTATION, CycleStatus.LACTATION)))
                .thenReturn(List.of(existingActive));

        assertThatThrownBy(() -> reproductiveCycleService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sow already has an active reproductive cycle");
    }

    @Test
    void getById_returnsCycle_whenSameFarm() {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setId(10);
        cycle.setSow(sow(5, farmA));
        cycle.setFarrowingNumber(1);
        cycle.setFarm(farmA);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));

        ReproductiveCycleDTO dto = reproductiveCycleService.getById(10);

        assertThat(dto.getId()).isEqualTo(10);
    }

    @Test
    void getById_throwsNotFound_whenDifferentFarm() {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setId(10);
        cycle.setSow(sow(5, farmB));
        cycle.setFarrowingNumber(1);
        cycle.setFarm(farmB);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> reproductiveCycleService.getById(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reproductive cycle not found");
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reproductiveCycleService.getById(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reproductive cycle not found");
    }

    @Test
    void list_withNoFilters_callsFindByFarmId() {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setId(1);
        cycle.setSow(sow(5, farmA));
        cycle.setFarrowingNumber(1);
        cycle.setFarm(farmA);

        when(reproductiveCycleRepository.findByFarmId(1)).thenReturn(List.of(cycle));

        List<ReproductiveCycleDTO> result = reproductiveCycleService.list(null, null);

        assertThat(result).hasSize(1);
        verify(reproductiveCycleRepository).findByFarmId(1);
    }

    @Test
    void list_withSowIdOnly_callsFindByFarmIdAndSowId() {
        when(reproductiveCycleRepository.findByFarmIdAndSowId(1, 5)).thenReturn(List.of());

        reproductiveCycleService.list(5, null);

        verify(reproductiveCycleRepository).findByFarmIdAndSowId(1, 5);
    }

    @Test
    void list_withStatusOnly_callsFindByFarmIdAndStatus() {
        when(reproductiveCycleRepository.findByFarmIdAndStatus(1, CycleStatus.GESTATION)).thenReturn(List.of());

        reproductiveCycleService.list(null, CycleStatus.GESTATION);

        verify(reproductiveCycleRepository).findByFarmIdAndStatus(1, CycleStatus.GESTATION);
    }

    @Test
    void list_withBothFilters_callsFindByFarmIdAndSowIdAndStatus() {
        when(reproductiveCycleRepository.findByFarmIdAndSowIdAndStatus(1, 5, CycleStatus.GESTATION))
                .thenReturn(List.of());

        reproductiveCycleService.list(5, CycleStatus.GESTATION);

        verify(reproductiveCycleRepository).findByFarmIdAndSowIdAndStatus(1, 5, CycleStatus.GESTATION);
    }

    @Test
    void update_preservesSowAndFarrowingNumber() {
        Animal sow = sow(5, farmA);
        ReproductiveCycle existing = new ReproductiveCycle();
        existing.setId(10);
        existing.setSow(sow);
        existing.setFarrowingNumber(3);
        existing.setFarm(farmA);
        existing.setStatus(CycleStatus.GESTATION);

        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();
        dto.setSowId(999);
        dto.setFarrowingNumber(999);
        dto.setStatus(CycleStatus.LACTATION);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(existing));
        when(reproductiveCycleRepository.save(any(ReproductiveCycle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReproductiveCycleDTO result = reproductiveCycleService.update(10, dto);

        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getSowId()).isEqualTo(5);
        assertThat(result.getFarrowingNumber()).isEqualTo(3);
        assertThat(result.getStatus()).isEqualTo(CycleStatus.LACTATION);
    }

    @Test
    void update_withOmittedFields_preservesExistingValues() {
        Animal sow = sow(5, farmA);
        ReproductiveCycle existing = new ReproductiveCycle();
        existing.setId(10);
        existing.setSow(sow);
        existing.setFarrowingNumber(1);
        existing.setFarm(farmA);
        existing.setStatus(CycleStatus.LACTATION);
        existing.setStartDate(java.time.LocalDate.of(2026, 1, 1));
        existing.setExpectedFarrowingDate(java.time.LocalDate.of(2026, 4, 25));

        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();
        dto.setActualFarrowingDate(java.time.LocalDate.of(2026, 4, 24));

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(existing));
        when(reproductiveCycleRepository.save(any(ReproductiveCycle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReproductiveCycleDTO result = reproductiveCycleService.update(10, dto);

        assertThat(result.getStatus()).isEqualTo(CycleStatus.LACTATION);
        assertThat(result.getStartDate()).isEqualTo(java.time.LocalDate.of(2026, 1, 1));
        assertThat(result.getExpectedFarrowingDate()).isEqualTo(java.time.LocalDate.of(2026, 4, 25));
        assertThat(result.getActualFarrowingDate()).isEqualTo(java.time.LocalDate.of(2026, 4, 24));
    }

    @Test
    void update_allowsAnyStatusTransitionWithoutValidation() {
        ReproductiveCycle existing = new ReproductiveCycle();
        existing.setId(10);
        existing.setSow(sow(5, farmA));
        existing.setFarrowingNumber(1);
        existing.setFarm(farmA);
        existing.setStatus(CycleStatus.GESTATION);

        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();
        dto.setStatus(CycleStatus.FINISHED);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(existing));
        when(reproductiveCycleRepository.save(any(ReproductiveCycle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReproductiveCycleDTO result = reproductiveCycleService.update(10, dto);

        assertThat(result.getStatus()).isEqualTo(CycleStatus.FINISHED);
    }

    @Test
    void update_throwsNotFound_whenDifferentFarm() {
        ReproductiveCycle existing = new ReproductiveCycle();
        existing.setId(10);
        existing.setSow(sow(5, farmB));
        existing.setFarrowingNumber(1);
        existing.setFarm(farmB);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reproductiveCycleService.update(10, new ReproductiveCycleDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reproductive cycle not found");
    }

    @Test
    void delete_removesCycle_whenSameFarm() {
        ReproductiveCycle existing = new ReproductiveCycle();
        existing.setId(10);
        existing.setSow(sow(5, farmA));
        existing.setFarrowingNumber(1);
        existing.setFarm(farmA);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(existing));

        reproductiveCycleService.delete(10);

        verify(reproductiveCycleRepository).delete(existing);
    }

    @Test
    void delete_throwsNotFound_whenDifferentFarm() {
        ReproductiveCycle existing = new ReproductiveCycle();
        existing.setId(10);
        existing.setSow(sow(5, farmB));
        existing.setFarrowingNumber(1);
        existing.setFarm(farmB);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reproductiveCycleService.delete(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reproductive cycle not found");
    }
}
