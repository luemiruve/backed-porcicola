package com.luemi.porcicola.mapper;

import com.luemi.porcicola.dto.MatingDTO;
import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Mating;
import com.luemi.porcicola.model.ReproductiveCycle;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MatingMapperTest {

    private final MatingMapper mapper = new MatingMapper();

    @Test
    void toDTO_mapsAllFieldsIncludingCycleIdAndBoarId() {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setId(10);

        Animal boar = new Animal();
        boar.setId(7);
        boar.setType(AnimalType.BOAR);

        Mating mating = new Mating();
        mating.setId(20);
        mating.setCycle(cycle);
        mating.setMatingDate(LocalDateTime.of(2026, 3, 1, 8, 0));
        mating.setIsInsemination(true);
        mating.setBoar(boar);

        MatingDTO dto = mapper.toDTO(mating);

        assertThat(dto.getId()).isEqualTo(20);
        assertThat(dto.getCycleId()).isEqualTo(10);
        assertThat(dto.getMatingDate()).isEqualTo(LocalDateTime.of(2026, 3, 1, 8, 0));
        assertThat(dto.getIsInsemination()).isTrue();
        assertThat(dto.getBoarId()).isEqualTo(7);
    }

    @Test
    void toDTO_withoutBoar_boarIdIsNull() {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setId(10);

        Mating mating = new Mating();
        mating.setId(21);
        mating.setCycle(cycle);
        mating.setMatingDate(LocalDateTime.of(2026, 3, 2, 9, 0));

        MatingDTO dto = mapper.toDTO(mating);

        assertThat(dto.getBoarId()).isNull();
    }

    @Test
    void toEntity_buildsMatingWithResolvedCycleAndBoar() {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setId(11);

        Animal boar = new Animal();
        boar.setId(8);
        boar.setType(AnimalType.BOAR);

        MatingDTO dto = new MatingDTO();
        dto.setMatingDate(LocalDateTime.of(2026, 4, 1, 10, 30));
        dto.setIsInsemination(false);

        Mating mating = mapper.toEntity(dto, cycle, boar);

        assertThat(mating.getCycle()).isSameAs(cycle);
        assertThat(mating.getBoar()).isSameAs(boar);
        assertThat(mating.getMatingDate()).isEqualTo(LocalDateTime.of(2026, 4, 1, 10, 30));
        assertThat(mating.getIsInsemination()).isFalse();
    }

    @Test
    void toEntity_withNullIsInsemination_keepsEntityDefaultFalse() {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setId(12);

        MatingDTO dto = new MatingDTO();
        dto.setMatingDate(LocalDateTime.of(2026, 4, 2, 11, 0));

        Mating mating = mapper.toEntity(dto, cycle, null);

        assertThat(mating.getIsInsemination()).isFalse();
        assertThat(mating.getBoar()).isNull();
    }
}
