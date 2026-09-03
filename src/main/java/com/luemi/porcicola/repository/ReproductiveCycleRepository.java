package com.luemi.porcicola.repository;

import com.luemi.porcicola.enums.CycleStatus;
import com.luemi.porcicola.model.ReproductiveCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReproductiveCycleRepository extends JpaRepository<ReproductiveCycle, Integer> {

    List<ReproductiveCycle> findByFarmId(Integer farmId);

    List<ReproductiveCycle> findByFarmIdAndSowId(Integer farmId, Integer sowId);

    List<ReproductiveCycle> findByFarmIdAndStatus(Integer farmId, CycleStatus status);

    List<ReproductiveCycle> findByFarmIdAndSowIdAndStatus(Integer farmId, Integer sowId, CycleStatus status);

    long countBySowId(Integer sowId);

    List<ReproductiveCycle> findBySowIdAndStatusIn(Integer sowId, List<CycleStatus> statuses);
}
