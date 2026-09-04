package com.luemi.pehuame.mapper;

import com.luemi.pehuame.dto.ReproductiveCycleDTO;
import com.luemi.pehuame.enums.AnimalType;
import com.luemi.pehuame.enums.CycleStatus;
import com.luemi.pehuame.model.Animal;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.model.ReproductiveCycle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReproductiveCycleMapperTest {

    private final ReproductiveCycleMapper mapper = new ReproductiveCycleMapper();

    @Test
    void toDTO_mapsAllFieldsIncludingSowId() {
        Farm farm = new Farm();
        farm.setId(1);

        Animal sow = new Animal();
        sow.setId(5);
        sow.setType(AnimalType.SOW);

        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setId(10);
        cycle.setSow(sow);
        cycle.setFarrowingNumber(2);
        cycle.setStartDate(LocalDate.of(2026, 1, 10));
        cycle.setExpectedFarrowingDate(LocalDate.of(2026, 5, 4));
        cycle.setActualFarrowingDate(null);
        cycle.setStatus(CycleStatus.GESTATION);
        cycle.setFarm(farm);

        ReproductiveCycleDTO dto = mapper.toDTO(cycle);

        assertThat(dto.getId()).isEqualTo(10);
        assertThat(dto.getSowId()).isEqualTo(5);
        assertThat(dto.getFarrowingNumber()).isEqualTo(2);
        assertThat(dto.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(dto.getExpectedFarrowingDate()).isEqualTo(LocalDate.of(2026, 5, 4));
        assertThat(dto.getActualFarrowingDate()).isNull();
        assertThat(dto.getStatus()).isEqualTo(CycleStatus.GESTATION);
    }

    @Test
    void toEntity_buildsCycleWithResolvedSowAndFarm() {
        Farm farm = new Farm();
        farm.setId(2);

        Animal sow = new Animal();
        sow.setId(7);
        sow.setType(AnimalType.SOW);

        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();
        dto.setStartDate(LocalDate.of(2026, 2, 1));
        dto.setExpectedFarrowingDate(LocalDate.of(2026, 5, 25));
        dto.setActualFarrowingDate(LocalDate.of(2026, 5, 24));
        dto.setStatus(CycleStatus.LACTATION);

        ReproductiveCycle cycle = mapper.toEntity(dto, sow, farm);

        assertThat(cycle.getSow()).isSameAs(sow);
        assertThat(cycle.getFarm()).isSameAs(farm);
        assertThat(cycle.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(cycle.getExpectedFarrowingDate()).isEqualTo(LocalDate.of(2026, 5, 25));
        assertThat(cycle.getActualFarrowingDate()).isEqualTo(LocalDate.of(2026, 5, 24));
        assertThat(cycle.getStatus()).isEqualTo(CycleStatus.LACTATION);
    }

    @Test
    void toEntity_withNullStatus_keepsEntityDefaultGestation() {
        Farm farm = new Farm();
        farm.setId(3);

        Animal sow = new Animal();
        sow.setId(8);
        sow.setType(AnimalType.SOW);

        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();

        ReproductiveCycle cycle = mapper.toEntity(dto, sow, farm);

        assertThat(cycle.getStatus()).isEqualTo(CycleStatus.GESTATION);
    }
}
