package com.luemi.pehuame.service;

import com.luemi.pehuame.dto.MatingDTO;
import com.luemi.pehuame.enums.AnimalType;
import com.luemi.pehuame.enums.CycleStatus;
import com.luemi.pehuame.enums.UserRole;
import com.luemi.pehuame.mapper.MatingMapper;
import com.luemi.pehuame.model.Animal;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.model.Mating;
import com.luemi.pehuame.model.ReproductiveCycle;
import com.luemi.pehuame.model.User;
import com.luemi.pehuame.repository.AnimalRepository;
import com.luemi.pehuame.repository.MatingRepository;
import com.luemi.pehuame.repository.ReproductiveCycleRepository;
import com.luemi.pehuame.security.UserPrincipal;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatingServiceTest {

    @Mock
    private MatingRepository matingRepository;

    @Mock
    private ReproductiveCycleRepository reproductiveCycleRepository;

    @Mock
    private AnimalRepository animalRepository;

    @Spy
    private MatingMapper matingMapper = new MatingMapper();

    @InjectMocks
    private MatingService matingService;

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

    private ReproductiveCycle cycle(Integer id, Farm farm, CycleStatus status) {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setId(id);
        cycle.setFarm(farm);
        cycle.setStatus(status);
        return cycle;
    }

    private Animal boar(Integer id, Farm farm) {
        Animal boar = new Animal();
        boar.setId(id);
        boar.setType(AnimalType.BOAR);
        boar.setFarm(farm);
        return boar;
    }

    @Test
    void create_savesMatingAndRecalculatesExpectedFarrowingDate() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        MatingDTO dto = new MatingDTO();
        dto.setCycleId(10);
        dto.setMatingDate(LocalDateTime.of(2026, 1, 10, 8, 0));

        Mating savedMating = new Mating();
        savedMating.setId(50);
        savedMating.setCycle(cycle);
        savedMating.setMatingDate(dto.getMatingDate());

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(matingRepository.save(any(Mating.class))).thenReturn(savedMating);
        when(matingRepository.findFirstByCycleIdOrderByMatingDateDesc(10)).thenReturn(Optional.of(savedMating));

        MatingDTO result = matingService.create(dto);

        assertThat(result.getId()).isEqualTo(50);
        assertThat(result.getCycleId()).isEqualTo(10);
        verify(reproductiveCycleRepository).save(cycle);
        assertThat(cycle.getExpectedFarrowingDate()).isEqualTo(LocalDate.of(2026, 1, 10).plusDays(114));
    }

    @Test
    void create_withNonexistentCycle_throws() {
        MatingDTO dto = new MatingDTO();
        dto.setCycleId(999);

        when(reproductiveCycleRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matingService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reproductive cycle not found");
    }

    @Test
    void create_withCycleFromAnotherFarm_throws() {
        ReproductiveCycle cycle = cycle(10, farmB, CycleStatus.GESTATION);
        MatingDTO dto = new MatingDTO();
        dto.setCycleId(10);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> matingService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reproductive cycle not found");
    }

    @Test
    void create_withCycleNotInGestation_throws() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.FINISHED);
        MatingDTO dto = new MatingDTO();
        dto.setCycleId(10);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> matingService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reproductive cycle not found");
    }

    @Test
    void create_withNonexistentBoar_throws() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        MatingDTO dto = new MatingDTO();
        dto.setCycleId(10);
        dto.setBoarId(999);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(animalRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matingService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Boar not found");
    }

    @Test
    void create_withBoarFromAnotherFarm_throws() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        Animal boarInOtherFarm = boar(5, farmB);
        MatingDTO dto = new MatingDTO();
        dto.setCycleId(10);
        dto.setBoarId(5);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(animalRepository.findById(5)).thenReturn(Optional.of(boarInOtherFarm));

        assertThatThrownBy(() -> matingService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Boar not found");
    }

    @Test
    void create_withNonBoarAnimal_throws() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        Animal notABoar = new Animal();
        notABoar.setId(5);
        notABoar.setType(AnimalType.SOW);
        notABoar.setFarm(farmA);
        MatingDTO dto = new MatingDTO();
        dto.setCycleId(10);
        dto.setBoarId(5);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(animalRepository.findById(5)).thenReturn(Optional.of(notABoar));

        assertThatThrownBy(() -> matingService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Boar not found");
    }

    @Test
    void create_withoutBoarId_allowed() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        MatingDTO dto = new MatingDTO();
        dto.setCycleId(10);
        dto.setMatingDate(LocalDateTime.of(2026, 2, 1, 8, 0));

        Mating savedMating = new Mating();
        savedMating.setId(51);
        savedMating.setCycle(cycle);
        savedMating.setMatingDate(dto.getMatingDate());

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(matingRepository.save(any(Mating.class))).thenReturn(savedMating);
        when(matingRepository.findFirstByCycleIdOrderByMatingDateDesc(10)).thenReturn(Optional.of(savedMating));

        MatingDTO result = matingService.create(dto);

        assertThat(result.getBoarId()).isNull();
    }

    @Test
    void getById_returnsMating_whenSameFarm() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        Mating mating = new Mating();
        mating.setId(50);
        mating.setCycle(cycle);
        mating.setMatingDate(LocalDateTime.of(2026, 1, 1, 8, 0));

        when(matingRepository.findById(50)).thenReturn(Optional.of(mating));

        MatingDTO dto = matingService.getById(50);

        assertThat(dto.getId()).isEqualTo(50);
    }

    @Test
    void getById_throwsNotFound_whenDifferentFarm() {
        ReproductiveCycle cycle = cycle(10, farmB, CycleStatus.GESTATION);
        Mating mating = new Mating();
        mating.setId(50);
        mating.setCycle(cycle);

        when(matingRepository.findById(50)).thenReturn(Optional.of(mating));

        assertThatThrownBy(() -> matingService.getById(50))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mating not found");
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(matingRepository.findById(50)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matingService.getById(50))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mating not found");
    }

    @Test
    void list_withCycleId_returnsMatings() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.FINISHED);
        Mating mating = new Mating();
        mating.setId(50);
        mating.setCycle(cycle);
        mating.setMatingDate(LocalDateTime.of(2026, 1, 1, 8, 0));

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(matingRepository.findByCycleId(10)).thenReturn(List.of(mating));

        List<MatingDTO> result = matingService.list(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(50);
    }

    @Test
    void list_withoutCycleId_throws() {
        assertThatThrownBy(() -> matingService.list(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("cycleId is required");
    }

    @Test
    void list_withCycleFromAnotherFarm_throws() {
        ReproductiveCycle cycle = cycle(10, farmB, CycleStatus.GESTATION);

        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));

        assertThatThrownBy(() -> matingService.list(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reproductive cycle not found");
    }

    @Test
    void update_preservesCycleId() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        Mating existing = new Mating();
        existing.setId(50);
        existing.setCycle(cycle);
        existing.setMatingDate(LocalDateTime.of(2026, 1, 1, 8, 0));

        MatingDTO dto = new MatingDTO();
        dto.setCycleId(999);
        dto.setMatingDate(LocalDateTime.of(2026, 1, 2, 9, 0));

        when(matingRepository.findById(50)).thenReturn(Optional.of(existing));
        when(matingRepository.save(any(Mating.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(matingRepository.findFirstByCycleIdOrderByMatingDateDesc(10)).thenReturn(Optional.of(existing));

        MatingDTO result = matingService.update(50, dto);

        assertThat(result.getCycleId()).isEqualTo(10);
    }

    @Test
    void update_throwsNotFound_whenDifferentFarm() {
        ReproductiveCycle cycle = cycle(10, farmB, CycleStatus.GESTATION);
        Mating existing = new Mating();
        existing.setId(50);
        existing.setCycle(cycle);

        when(matingRepository.findById(50)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> matingService.update(50, new MatingDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mating not found");
    }

    @Test
    void update_recalculatesExpectedFarrowingDate() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        Mating existing = new Mating();
        existing.setId(50);
        existing.setCycle(cycle);
        existing.setMatingDate(LocalDateTime.of(2026, 1, 1, 8, 0));

        MatingDTO dto = new MatingDTO();
        dto.setMatingDate(LocalDateTime.of(2026, 1, 10, 8, 0));

        Mating updated = new Mating();
        updated.setId(50);
        updated.setCycle(cycle);
        updated.setMatingDate(dto.getMatingDate());

        when(matingRepository.findById(50)).thenReturn(Optional.of(existing));
        when(matingRepository.save(any(Mating.class))).thenReturn(updated);
        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(matingRepository.findFirstByCycleIdOrderByMatingDateDesc(10)).thenReturn(Optional.of(updated));

        matingService.update(50, dto);

        assertThat(cycle.getExpectedFarrowingDate()).isEqualTo(LocalDate.of(2026, 1, 10).plusDays(114));
    }

    @Test
    void delete_removesMating_whenSameFarm() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        Mating existing = new Mating();
        existing.setId(50);
        existing.setCycle(cycle);

        when(matingRepository.findById(50)).thenReturn(Optional.of(existing));
        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(matingRepository.findFirstByCycleIdOrderByMatingDateDesc(10)).thenReturn(Optional.empty());

        matingService.delete(50);

        verify(matingRepository).delete(existing);
    }

    @Test
    void delete_throwsNotFound_whenDifferentFarm() {
        ReproductiveCycle cycle = cycle(10, farmB, CycleStatus.GESTATION);
        Mating existing = new Mating();
        existing.setId(50);
        existing.setCycle(cycle);

        when(matingRepository.findById(50)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> matingService.delete(50))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mating not found");
    }

    @Test
    void delete_lastMating_clearsExpectedFarrowingDate() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        cycle.setExpectedFarrowingDate(LocalDate.of(2026, 5, 1));
        Mating existing = new Mating();
        existing.setId(50);
        existing.setCycle(cycle);

        when(matingRepository.findById(50)).thenReturn(Optional.of(existing));
        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(matingRepository.findFirstByCycleIdOrderByMatingDateDesc(10)).thenReturn(Optional.empty());

        matingService.delete(50);

        assertThat(cycle.getExpectedFarrowingDate()).isNull();
    }

    @Test
    void delete_remainingMating_recalculatesToItsDate() {
        ReproductiveCycle cycle = cycle(10, farmA, CycleStatus.GESTATION);
        Mating toDelete = new Mating();
        toDelete.setId(50);
        toDelete.setCycle(cycle);

        Mating remaining = new Mating();
        remaining.setId(51);
        remaining.setCycle(cycle);
        remaining.setMatingDate(LocalDateTime.of(2026, 2, 1, 8, 0));

        when(matingRepository.findById(50)).thenReturn(Optional.of(toDelete));
        when(reproductiveCycleRepository.findById(10)).thenReturn(Optional.of(cycle));
        when(matingRepository.findFirstByCycleIdOrderByMatingDateDesc(10)).thenReturn(Optional.of(remaining));

        matingService.delete(50);

        assertThat(cycle.getExpectedFarrowingDate()).isEqualTo(LocalDate.of(2026, 2, 1).plusDays(114));
    }
}
