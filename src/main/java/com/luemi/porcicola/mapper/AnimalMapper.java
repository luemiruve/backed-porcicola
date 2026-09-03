package com.luemi.porcicola.mapper;

import com.luemi.porcicola.dto.AnimalDTO;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Farm;
import org.springframework.stereotype.Component;

@Component
public class AnimalMapper {

    public AnimalDTO toDTO(Animal animal) {
        AnimalDTO dto = new AnimalDTO();
        dto.setId(animal.getId());
        dto.setNfcUid(animal.getNfcUid());
        dto.setName(animal.getName());
        dto.setType(animal.getType());
        dto.setBirthDate(animal.getBirthDate());
        dto.setStatus(animal.getStatus());
        dto.setMotherId(animal.getMother() != null ? animal.getMother().getId() : null);
        dto.setCurrentWeight(animal.getCurrentWeight());
        dto.setNotes(animal.getNotes());
        return dto;
    }

    public Animal toEntity(AnimalDTO dto, Animal mother, Farm farm) {
        Animal animal = new Animal();
        animal.setNfcUid(dto.getNfcUid());
        animal.setName(dto.getName());
        animal.setType(dto.getType());
        animal.setBirthDate(dto.getBirthDate());
        if (dto.getStatus() != null) {
            animal.setStatus(dto.getStatus());
        }
        animal.setMother(mother);
        animal.setCurrentWeight(dto.getCurrentWeight());
        animal.setNotes(dto.getNotes());
        animal.setFarm(farm);
        return animal;
    }
}
