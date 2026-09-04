package com.luemi.pehuame.repository;

import com.luemi.pehuame.enums.AnimalStatus;
import com.luemi.pehuame.enums.AnimalType;
import com.luemi.pehuame.model.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Integer> {

    List<Animal> findByFarmId(Integer farmId);

    List<Animal> findByFarmIdAndType(Integer farmId, AnimalType type);

    List<Animal> findByFarmIdAndStatus(Integer farmId, AnimalStatus status);

    List<Animal> findByFarmIdAndTypeAndStatus(Integer farmId, AnimalType type, AnimalStatus status);
}
