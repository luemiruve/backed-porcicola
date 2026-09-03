package com.luemi.porcicola.service;

import com.luemi.porcicola.dto.FarmDTO;
import com.luemi.porcicola.model.Farm;
import com.luemi.porcicola.repository.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// service/FarmService.java
@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    public Farm getById(Integer farmId) {
        return farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));
    }

    public Farm update(Integer farmId, FarmDTO dto) {
        Farm farm = getById(farmId);
        farm.setName(dto.getName());
        farm.setLocation(dto.getLocation());
        return farmRepository.save(farm);
    }
}