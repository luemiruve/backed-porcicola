package com.luemi.pehuame.service;

import com.luemi.pehuame.dto.AnimalDTO;
import com.luemi.pehuame.enums.AnimalStatus;
import com.luemi.pehuame.enums.AnimalType;
import com.luemi.pehuame.mapper.AnimalMapper;
import com.luemi.pehuame.model.Animal;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.repository.AnimalRepository;
import com.luemi.pehuame.repository.FarmRepository;
import com.luemi.pehuame.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnimalService {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private AnimalMapper animalMapper;

    @Transactional
    public AnimalDTO create(AnimalDTO dto) {
        Integer farmId = CurrentUser.get().getFarmId();
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));
        Animal mother = resolveMother(dto.getMotherId(), farmId);

        Animal animal = animalMapper.toEntity(dto, mother, farm);

        return animalMapper.toDTO(animalRepository.save(animal));
    }

    public AnimalDTO getById(Integer id) {
        return animalMapper.toDTO(getOwnedAnimal(id));
    }

    public List<AnimalDTO> list(AnimalType type, AnimalStatus status) {
        Integer farmId = CurrentUser.get().getFarmId();
        List<Animal> animals;
        if (type != null && status != null) {
            animals = animalRepository.findByFarmIdAndTypeAndStatus(farmId, type, status);
        } else if (type != null) {
            animals = animalRepository.findByFarmIdAndType(farmId, type);
        } else if (status != null) {
            animals = animalRepository.findByFarmIdAndStatus(farmId, status);
        } else {
            animals = animalRepository.findByFarmId(farmId);
        }
        return animals.stream()
                .map(animalMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnimalDTO update(Integer id, AnimalDTO dto) {
        Animal existing = getOwnedAnimal(id);
        Animal mother = resolveMother(dto.getMotherId(), existing.getFarm().getId());

        Animal animal = animalMapper.toEntity(dto, mother, existing.getFarm());
        animal.setId(existing.getId());

        return animalMapper.toDTO(animalRepository.save(animal));
    }

    @Transactional
    public void delete(Integer id) {
        Animal animal = getOwnedAnimal(id);
        animalRepository.delete(animal);
    }

    private Animal getOwnedAnimal(Integer id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal not found"));
        if (!animal.getFarm().getId().equals(CurrentUser.get().getFarmId())) {
            throw new RuntimeException("Animal not found");
        }
        return animal;
    }

    private Animal resolveMother(Integer motherId, Integer farmId) {
        if (motherId == null) {
            return null;
        }
        Animal mother = animalRepository.findById(motherId)
                .orElseThrow(() -> new RuntimeException("Mother not found"));
        if (!mother.getFarm().getId().equals(farmId)) {
            throw new RuntimeException("Mother not found");
        }
        return mother;
    }
}
