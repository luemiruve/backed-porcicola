package com.luemi.porcicola.mapper;

import com.luemi.porcicola.dto.ReproductiveCycleDTO;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Farm;
import com.luemi.porcicola.model.ReproductiveCycle;
import org.springframework.stereotype.Component;

@Component
public class ReproductiveCycleMapper {

    public ReproductiveCycleDTO toDTO(ReproductiveCycle cycle) {
        ReproductiveCycleDTO dto = new ReproductiveCycleDTO();
        dto.setId(cycle.getId());
        dto.setSowId(cycle.getSow().getId());
        dto.setFarrowingNumber(cycle.getFarrowingNumber());
        dto.setStartDate(cycle.getStartDate());
        dto.setExpectedFarrowingDate(cycle.getExpectedFarrowingDate());
        dto.setActualFarrowingDate(cycle.getActualFarrowingDate());
        dto.setStatus(cycle.getStatus());
        return dto;
    }

    public ReproductiveCycle toEntity(ReproductiveCycleDTO dto, Animal sow, Farm farm) {
        ReproductiveCycle cycle = new ReproductiveCycle();
        cycle.setSow(sow);
        cycle.setStartDate(dto.getStartDate());
        cycle.setExpectedFarrowingDate(dto.getExpectedFarrowingDate());
        cycle.setActualFarrowingDate(dto.getActualFarrowingDate());
        if (dto.getStatus() != null) {
            cycle.setStatus(dto.getStatus());
        }
        cycle.setFarm(farm);
        return cycle;
    }
}
