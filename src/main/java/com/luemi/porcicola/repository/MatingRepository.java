package com.luemi.porcicola.repository;

import com.luemi.porcicola.model.Mating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatingRepository extends JpaRepository<Mating, Integer> {

    List<Mating> findByCycleId(Integer cycleId);

    Optional<Mating> findFirstByCycleIdOrderByMatingDateDesc(Integer cycleId);
}
