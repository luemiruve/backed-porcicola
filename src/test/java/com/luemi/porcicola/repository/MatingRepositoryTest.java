package com.luemi.porcicola.repository;

import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.enums.CycleStatus;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Farm;
import com.luemi.porcicola.model.Mating;
import com.luemi.porcicola.model.ReproductiveCycle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MatingRepositoryTest {

    @Autowired
    private MatingRepository matingRepository;

    @Autowired
    private ReproductiveCycleRepository reproductiveCycleRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private AnimalRepository animalRepository;

    private Farm createFarm() {
        Farm farm = new Farm();
        farm.setName("Test Farm");
        return farmRepository.save(farm);
    }

    private Animal createSow(Farm farm) {
        Animal sow = new Animal();
        sow.setType(AnimalType.SOW);
        sow.setFarm(farm);
        return animalRepository.save(sow);
    }

    private Animal createBoar(Farm farm) {
        Animal boar = new Animal();
        boar.setType(AnimalType.BOAR);
        boar.setFarm(farm);
        return animalRepository.save(boar);
    }

    private ReproductiveCycle createCycle(Farm farm, Animal sow) {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setFarm(farm);
        cycle.setSow(sow);
        cycle.setFarrowingNumber(1);
        cycle.setStatus(CycleStatus.GESTATION);
        return reproductiveCycleRepository.save(cycle);
    }

    private Mating createMating(ReproductiveCycle cycle, Animal boar, LocalDateTime matingDate) {
        Mating mating = new Mating();
        mating.setCycle(cycle);
        mating.setBoar(boar);
        mating.setMatingDate(matingDate);
        return matingRepository.save(mating);
    }

    @Test
    void findByCycleId_returnsOnlyMatingsForThatCycle() {
        Farm farm = createFarm();
        Animal sow1 = createSow(farm);
        Animal sow2 = createSow(farm);
        ReproductiveCycle cycle1 = createCycle(farm, sow1);
        ReproductiveCycle cycle2 = createCycle(farm, sow2);
        createMating(cycle1, null, LocalDateTime.of(2026, 1, 1, 8, 0));
        createMating(cycle2, null, LocalDateTime.of(2026, 1, 2, 8, 0));

        List<Mating> result = matingRepository.findByCycleId(cycle1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCycle().getId()).isEqualTo(cycle1.getId());
    }

    @Test
    void findFirstByCycleIdOrderByMatingDateDesc_returnsMostRecent() {
        Farm farm = createFarm();
        Animal sow = createSow(farm);
        Animal boar = createBoar(farm);
        ReproductiveCycle cycle = createCycle(farm, sow);
        createMating(cycle, boar, LocalDateTime.of(2026, 1, 1, 8, 0));
        createMating(cycle, boar, LocalDateTime.of(2026, 1, 5, 8, 0));

        Optional<Mating> result = matingRepository.findFirstByCycleIdOrderByMatingDateDesc(cycle.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getMatingDate()).isEqualTo(LocalDateTime.of(2026, 1, 5, 8, 0));
    }

    @Test
    void findFirstByCycleIdOrderByMatingDateDesc_returnsEmpty_whenNoMatings() {
        Farm farm = createFarm();
        Animal sow = createSow(farm);
        ReproductiveCycle cycle = createCycle(farm, sow);

        Optional<Mating> result = matingRepository.findFirstByCycleIdOrderByMatingDateDesc(cycle.getId());

        assertThat(result).isEmpty();
    }
}
