package com.luemi.porcicola.mapper;

import com.luemi.porcicola.dto.MatingDTO;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Mating;
import com.luemi.porcicola.model.ReproductiveCycle;
import org.springframework.stereotype.Component;

@Component
public class MatingMapper {

    public MatingDTO toDTO(Mating mating) {
        MatingDTO dto = new MatingDTO();
        dto.setId(mating.getId());
        dto.setCycleId(mating.getCycle().getId());
        dto.setMatingDate(mating.getMatingDate());
        dto.setIsInsemination(mating.getIsInsemination());
        dto.setBoarId(mating.getBoar() != null ? mating.getBoar().getId() : null);
        return dto;
    }

    public Mating toEntity(MatingDTO dto, ReproductiveCycle cycle, Animal boar) {
        Mating mating = new Mating();
        mating.setCycle(cycle);
        mating.setMatingDate(dto.getMatingDate());
        if (dto.getIsInsemination() != null) {
            mating.setIsInsemination(dto.getIsInsemination());
        }
        mating.setBoar(boar);
        return mating;
    }
}
