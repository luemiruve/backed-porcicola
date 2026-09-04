package com.luemi.pehuame.service;

import com.luemi.pehuame.dto.AnimalDTO;
import com.luemi.pehuame.enums.AnimalStatus;
import com.luemi.pehuame.enums.AnimalType;
import com.luemi.pehuame.enums.UserRole;
import com.luemi.pehuame.mapper.AnimalMapper;
import com.luemi.pehuame.model.Animal;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.model.User;
import com.luemi.pehuame.repository.AnimalRepository;
import com.luemi.pehuame.repository.FarmRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private FarmRepository farmRepository;

    @Spy
    private AnimalMapper animalMapper = new AnimalMapper();

    @InjectMocks
    private AnimalService animalService;

    private Farm farmA;
    private Farm farmB;

    @BeforeEach
    void setUp() {
        farmA = new Farm();
        farmA.setId(1);

        farmB = new Farm();
        farmB.setId(2);

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
    void create_assignsCurrentUserFarm() {
        AnimalDTO dto = new AnimalDTO();
        dto.setType(AnimalType.SOW);
        dto.setName("Bella");

        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> {
            Animal saved = invocation.getArgument(0);
            saved.setId(100);
            return saved;
        });

        AnimalDTO result = animalService.create(dto);

        assertThat(result.getId()).isEqualTo(100);
        assertThat(result.getName()).isEqualTo("Bella");
    }

    @Test
    void create_withMotherFromAnotherFarm_throws() {
        AnimalDTO dto = new AnimalDTO();
        dto.setType(AnimalType.PIGLET);
        dto.setMotherId(5);

        Animal motherInOtherFarm = new Animal();
        motherInOtherFarm.setId(5);
        motherInOtherFarm.setFarm(farmB);

        when(farmRepository.findById(1)).thenReturn(Optional.of(farmA));
        when(animalRepository.findById(5)).thenReturn(Optional.of(motherInOtherFarm));

        assertThatThrownBy(() -> animalService.create(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mother not found");
    }

    @Test
    void getById_returnsAnimal_whenSameFarm() {
        Animal animal = new Animal();
        animal.setId(10);
        animal.setType(AnimalType.SOW);
        animal.setFarm(farmA);

        when(animalRepository.findById(10)).thenReturn(Optional.of(animal));

        AnimalDTO dto = animalService.getById(10);

        assertThat(dto.getId()).isEqualTo(10);
    }

    @Test
    void getById_throwsAnimalNotFound_whenDifferentFarm() {
        Animal animal = new Animal();
        animal.setId(10);
        animal.setType(AnimalType.SOW);
        animal.setFarm(farmB);

        when(animalRepository.findById(10)).thenReturn(Optional.of(animal));

        assertThatThrownBy(() -> animalService.getById(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Animal not found");
    }

    @Test
    void getById_throwsAnimalNotFound_whenMissing() {
        when(animalRepository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.getById(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Animal not found");
    }

    @Test
    void list_withNoFilters_callsFindByFarmId() {
        Animal animal = new Animal();
        animal.setId(1);
        animal.setType(AnimalType.SOW);
        animal.setFarm(farmA);

        when(animalRepository.findByFarmId(1)).thenReturn(List.of(animal));

        List<AnimalDTO> result = animalService.list(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        verify(animalRepository).findByFarmId(1);
    }

    @Test
    void list_withTypeOnly_callsFindByFarmIdAndType() {
        Animal animal = new Animal();
        animal.setId(1);
        animal.setType(AnimalType.SOW);
        animal.setFarm(farmA);

        when(animalRepository.findByFarmIdAndType(1, AnimalType.SOW)).thenReturn(List.of(animal));

        List<AnimalDTO> result = animalService.list(AnimalType.SOW, null);

        assertThat(result).hasSize(1);
        verify(animalRepository).findByFarmIdAndType(1, AnimalType.SOW);
    }

    @Test
    void list_withStatusOnly_callsFindByFarmIdAndStatus() {
        Animal animal = new Animal();
        animal.setId(1);
        animal.setType(AnimalType.SOW);
        animal.setFarm(farmA);

        when(animalRepository.findByFarmIdAndStatus(1, AnimalStatus.ACTIVE)).thenReturn(List.of(animal));

        List<AnimalDTO> result = animalService.list(null, AnimalStatus.ACTIVE);

        assertThat(result).hasSize(1);
        verify(animalRepository).findByFarmIdAndStatus(1, AnimalStatus.ACTIVE);
    }

    @Test
    void list_filtersByCurrentUserFarmTypeAndStatus() {
        Animal animal = new Animal();
        animal.setId(1);
        animal.setType(AnimalType.SOW);
        animal.setFarm(farmA);

        when(animalRepository.findByFarmIdAndTypeAndStatus(1, AnimalType.SOW, AnimalStatus.ACTIVE))
                .thenReturn(List.of(animal));

        List<AnimalDTO> result = animalService.list(AnimalType.SOW, AnimalStatus.ACTIVE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        verify(animalRepository).findByFarmIdAndTypeAndStatus(1, AnimalType.SOW, AnimalStatus.ACTIVE);
    }

    @Test
    void update_appliesChanges_whenSameFarm() {
        Animal existing = new Animal();
        existing.setId(10);
        existing.setType(AnimalType.SOW);
        existing.setName("Old name");
        existing.setFarm(farmA);

        AnimalDTO dto = new AnimalDTO();
        dto.setType(AnimalType.SOW);
        dto.setName("New name");

        when(animalRepository.findById(10)).thenReturn(Optional.of(existing));
        when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnimalDTO result = animalService.update(10, dto);

        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getName()).isEqualTo("New name");
    }

    @Test
    void update_throwsAnimalNotFound_whenDifferentFarm() {
        Animal existing = new Animal();
        existing.setId(10);
        existing.setType(AnimalType.SOW);
        existing.setFarm(farmB);

        when(animalRepository.findById(10)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> animalService.update(10, new AnimalDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Animal not found");
    }

    @Test
    void delete_removesAnimal_whenSameFarm() {
        Animal existing = new Animal();
        existing.setId(10);
        existing.setType(AnimalType.SOW);
        existing.setFarm(farmA);

        when(animalRepository.findById(10)).thenReturn(Optional.of(existing));

        animalService.delete(10);

        verify(animalRepository).delete(existing);
    }

    @Test
    void delete_throwsAnimalNotFound_whenDifferentFarm() {
        Animal existing = new Animal();
        existing.setId(10);
        existing.setType(AnimalType.SOW);
        existing.setFarm(farmB);

        when(animalRepository.findById(10)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> animalService.delete(10))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Animal not found");
    }
}
