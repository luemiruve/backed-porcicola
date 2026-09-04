package com.luemi.pehuame.repository;

import com.luemi.pehuame.enums.AnimalType;
import com.luemi.pehuame.enums.CycleStatus;
import com.luemi.pehuame.model.Animal;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.model.ReproductiveCycle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReproductiveCycleRepositoryTest {

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

    private ReproductiveCycle createCycle(Farm farm, Animal sow, Integer farrowingNumber, CycleStatus status) {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setFarm(farm);
        cycle.setSow(sow);
        cycle.setFarrowingNumber(farrowingNumber);
        cycle.setStatus(status);
        return reproductiveCycleRepository.save(cycle);
    }

    @Test
    void findByFarmId_returnsOnlyCyclesForThatFarm() {
        Farm farmA = createFarm();
        Farm farmB = createFarm();
        Animal sowA = createSow(farmA);
        Animal sowB = createSow(farmB);
        createCycle(farmA, sowA, 1, CycleStatus.GESTATION);
        createCycle(farmB, sowB, 1, CycleStatus.GESTATION);

        List<ReproductiveCycle> result = reproductiveCycleRepository.findByFarmId(farmA.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSow().getId()).isEqualTo(sowA.getId());
    }

    @Test
    void findByFarmIdAndSowId_filtersBySow() {
        Farm farm = createFarm();
        Animal sow1 = createSow(farm);
        Animal sow2 = createSow(farm);
        createCycle(farm, sow1, 1, CycleStatus.GESTATION);
        createCycle(farm, sow2, 1, CycleStatus.GESTATION);

        List<ReproductiveCycle> result = reproductiveCycleRepository.findByFarmIdAndSowId(farm.getId(), sow1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSow().getId()).isEqualTo(sow1.getId());
    }

    @Test
    void findByFarmIdAndStatus_filtersByStatus() {
        Farm farm = createFarm();
        Animal sow = createSow(farm);
        createCycle(farm, sow, 1, CycleStatus.GESTATION);
        createCycle(farm, sow, 2, CycleStatus.FINISHED);

        List<ReproductiveCycle> result = reproductiveCycleRepository.findByFarmIdAndStatus(farm.getId(), CycleStatus.GESTATION);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(CycleStatus.GESTATION);
    }

    @Test
    void findByFarmIdAndSowIdAndStatus_combinesFilters() {
        Farm farm = createFarm();
        Animal sow = createSow(farm);
        createCycle(farm, sow, 1, CycleStatus.GESTATION);
        createCycle(farm, sow, 2, CycleStatus.FINISHED);

        List<ReproductiveCycle> result = reproductiveCycleRepository.findByFarmIdAndSowIdAndStatus(
                farm.getId(), sow.getId(), CycleStatus.FINISHED);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFarrowingNumber()).isEqualTo(2);
    }

    @Test
    void findFirstBySowIdOrderByFarrowingNumberDesc_returnsHighestFarrowingNumber() {
        Farm farm = createFarm();
        Animal sow = createSow(farm);
        createCycle(farm, sow, 1, CycleStatus.FINISHED);
        createCycle(farm, sow, 2, CycleStatus.GESTATION);

        Optional<ReproductiveCycle> result = reproductiveCycleRepository.findFirstBySowIdOrderByFarrowingNumberDesc(sow.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getFarrowingNumber()).isEqualTo(2);
    }

    @Test
    void findFirstBySowIdOrderByFarrowingNumberDesc_returnsEmpty_whenSowHasNoCycles() {
        Farm farm = createFarm();
        Animal sow = createSow(farm);

        Optional<ReproductiveCycle> result = reproductiveCycleRepository.findFirstBySowIdOrderByFarrowingNumberDesc(sow.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findBySowIdAndStatusIn_returnsOnlyMatchingStatuses() {
        Farm farm = createFarm();
        Animal sow = createSow(farm);
        createCycle(farm, sow, 1, CycleStatus.FINISHED);
        createCycle(farm, sow, 2, CycleStatus.LACTATION);

        List<ReproductiveCycle> active = reproductiveCycleRepository.findBySowIdAndStatusIn(
                sow.getId(), List.of(CycleStatus.GESTATION, CycleStatus.LACTATION));

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getStatus()).isEqualTo(CycleStatus.LACTATION);
    }
}
