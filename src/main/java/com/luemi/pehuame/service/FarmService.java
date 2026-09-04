package com.luemi.pehuame.service;

import com.luemi.pehuame.dto.FarmDTO;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.repository.FarmRepository;
import com.luemi.pehuame.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// service/FarmService.java
@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    public Farm getById(Integer farmId) {
        return getOwnedFarm(farmId);
    }

    public Farm update(Integer farmId, FarmDTO dto) {
        Farm farm = getOwnedFarm(farmId);
        farm.setName(dto.getName());
        farm.setLocation(dto.getLocation());
        return farmRepository.save(farm);
    }

    private Farm getOwnedFarm(Integer farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));
        if (!farm.getId().equals(CurrentUser.get().getFarmId())) {
            throw new RuntimeException("Farm not found");
        }
        return farm;
    }
}
