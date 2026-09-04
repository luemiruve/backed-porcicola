package com.luemi.pehuame.service;

import com.luemi.pehuame.dto.ReproductiveCycleDTO;
import com.luemi.pehuame.enums.AnimalType;
import com.luemi.pehuame.enums.CycleStatus;
import com.luemi.pehuame.mapper.ReproductiveCycleMapper;
import com.luemi.pehuame.model.Animal;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.model.ReproductiveCycle;
import com.luemi.pehuame.repository.AnimalRepository;
import com.luemi.pehuame.repository.FarmRepository;
import com.luemi.pehuame.repository.ReproductiveCycleRepository;
import com.luemi.pehuame.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReproductiveCycleService {

    @Autowired
    private ReproductiveCycleRepository reproductiveCycleRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private ReproductiveCycleMapper reproductiveCycleMapper;

    @Transactional
    public ReproductiveCycleDTO create(ReproductiveCycleDTO dto) {
        Integer farmId = CurrentUser.get().getFarmId();
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));
        Animal sow = resolveSow(dto.getSowId(), farmId);

        List<ReproductiveCycle> activeCycles = reproductiveCycleRepository.findBySowIdAndStatusIn(
                sow.getId(), List.of(CycleStatus.GESTATION, CycleStatus.LACTATION));
        if (!activeCycles.isEmpty()) {
            throw new RuntimeException("Sow already has an active reproductive cycle");
        }

        ReproductiveCycle cycle = reproductiveCycleMapper.toEntity(dto, sow, farm);
        int nextFarrowingNumber = reproductiveCycleRepository.findFirstBySowIdOrderByFarrowingNumberDesc(sow.getId())
                .map(previous -> previous.getFarrowingNumber() + 1)
                .orElse(1);
        cycle.setFarrowingNumber(nextFarrowingNumber);

        return reproductiveCycleMapper.toDTO(reproductiveCycleRepository.save(cycle));
    }

    public ReproductiveCycleDTO getById(Integer id) {
        return reproductiveCycleMapper.toDTO(getOwnedCycle(id));
    }

    public List<ReproductiveCycleDTO> list(Integer sowId, CycleStatus status) {
        Integer farmId = CurrentUser.get().getFarmId();
        List<ReproductiveCycle> cycles;
        if (sowId != null && status != null) {
            cycles = reproductiveCycleRepository.findByFarmIdAndSowIdAndStatus(farmId, sowId, status);
        } else if (sowId != null) {
            cycles = reproductiveCycleRepository.findByFarmIdAndSowId(farmId, sowId);
        } else if (status != null) {
            cycles = reproductiveCycleRepository.findByFarmIdAndStatus(farmId, status);
        } else {
            cycles = reproductiveCycleRepository.findByFarmId(farmId);
        }
        return cycles.stream()
                .map(reproductiveCycleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReproductiveCycleDTO update(Integer id, ReproductiveCycleDTO dto) {
        ReproductiveCycle existing = getOwnedCycle(id);

        if (dto.getStartDate() != null) {
            existing.setStartDate(dto.getStartDate());
        }
        if (dto.getExpectedFarrowingDate() != null) {
            existing.setExpectedFarrowingDate(dto.getExpectedFarrowingDate());
        }
        if (dto.getActualFarrowingDate() != null) {
            existing.setActualFarrowingDate(dto.getActualFarrowingDate());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        return reproductiveCycleMapper.toDTO(reproductiveCycleRepository.save(existing));
    }

    @Transactional
    public void delete(Integer id) {
        ReproductiveCycle cycle = getOwnedCycle(id);
        reproductiveCycleRepository.delete(cycle);
    }

    private ReproductiveCycle getOwnedCycle(Integer id) {
        ReproductiveCycle cycle = reproductiveCycleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reproductive cycle not found"));
        if (!cycle.getFarm().getId().equals(CurrentUser.get().getFarmId())) {
            throw new RuntimeException("Reproductive cycle not found");
        }
        return cycle;
    }

    private Animal resolveSow(Integer sowId, Integer farmId) {
        if (sowId == null) {
            throw new RuntimeException("Sow not found");
        }
        Animal sow = animalRepository.findById(sowId)
                .orElseThrow(() -> new RuntimeException("Sow not found"));
        if (!sow.getFarm().getId().equals(farmId) || sow.getType() != AnimalType.SOW) {
            throw new RuntimeException("Sow not found");
        }
        return sow;
    }
}
