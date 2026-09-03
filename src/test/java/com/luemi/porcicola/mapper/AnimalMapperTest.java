package com.luemi.porcicola.mapper;

import com.luemi.porcicola.dto.AnimalDTO;
import com.luemi.porcicola.enums.AnimalStatus;
import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Farm;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalMapperTest {

    private final AnimalMapper mapper = new AnimalMapper();

    @Test
    void toDTO_mapsAllFieldsIncludingMotherId() {
        Farm farm = new Farm();
        farm.setId(1);

        Animal mother = new Animal();
        mother.setId(5);

        Animal animal = new Animal();
        animal.setId(10);
        animal.setNfcUid("NFC-1");
        animal.setName("Bella");
        animal.setType(AnimalType.SOW);
        animal.setBirthDate(LocalDate.of(2025, 1, 15));
        animal.setStatus(AnimalStatus.ACTIVE);
        animal.setMother(mother);
        animal.setCurrentWeight(new BigDecimal("120.50"));
        animal.setNotes("Healthy");
        animal.setFarm(farm);

        AnimalDTO dto = mapper.toDTO(animal);

        assertThat(dto.getId()).isEqualTo(10);
        assertThat(dto.getNfcUid()).isEqualTo("NFC-1");
        assertThat(dto.getName()).isEqualTo("Bella");
        assertThat(dto.getType()).isEqualTo(AnimalType.SOW);
        assertThat(dto.getBirthDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(dto.getStatus()).isEqualTo(AnimalStatus.ACTIVE);
        assertThat(dto.getMotherId()).isEqualTo(5);
        assertThat(dto.getCurrentWeight()).isEqualTo(new BigDecimal("120.50"));
        assertThat(dto.getNotes()).isEqualTo("Healthy");
    }

    @Test
    void toDTO_withoutMother_motherIdIsNull() {
        Animal animal = new Animal();
        animal.setId(11);
        animal.setType(AnimalType.PIGLET);

        AnimalDTO dto = mapper.toDTO(animal);

        assertThat(dto.getMotherId()).isNull();
    }

    @Test
    void toEntity_buildsAnimalWithResolvedMotherAndFarm() {
        Farm farm = new Farm();
        farm.setId(2);

        Animal mother = new Animal();
        mother.setId(7);

        AnimalDTO dto = new AnimalDTO();
        dto.setNfcUid("NFC-2");
        dto.setName("Rocky");
        dto.setType(AnimalType.BOAR);
        dto.setBirthDate(LocalDate.of(2024, 6, 1));
        dto.setStatus(AnimalStatus.FATTENING);
        dto.setCurrentWeight(new BigDecimal("80.00"));
        dto.setNotes("None");

        Animal animal = mapper.toEntity(dto, mother, farm);

        assertThat(animal.getNfcUid()).isEqualTo("NFC-2");
        assertThat(animal.getName()).isEqualTo("Rocky");
        assertThat(animal.getType()).isEqualTo(AnimalType.BOAR);
        assertThat(animal.getBirthDate()).isEqualTo(LocalDate.of(2024, 6, 1));
        assertThat(animal.getStatus()).isEqualTo(AnimalStatus.FATTENING);
        assertThat(animal.getMother()).isSameAs(mother);
        assertThat(animal.getCurrentWeight()).isEqualTo(new BigDecimal("80.00"));
        assertThat(animal.getNotes()).isEqualTo("None");
        assertThat(animal.getFarm()).isSameAs(farm);
    }

    @Test
    void toEntity_withNullStatus_keepsEntityDefaultActive() {
        Farm farm = new Farm();
        farm.setId(3);

        AnimalDTO dto = new AnimalDTO();
        dto.setType(AnimalType.PIGLET);

        Animal animal = mapper.toEntity(dto, null, farm);

        assertThat(animal.getStatus()).isEqualTo(AnimalStatus.ACTIVE);
    }
}
