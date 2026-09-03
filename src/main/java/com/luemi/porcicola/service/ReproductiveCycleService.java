package com.luemi.porcicola.service;

import com.luemi.porcicola.dto.ReproductiveCycleDTO;
import com.luemi.porcicola.enums.AnimalType;
import com.luemi.porcicola.enums.CycleStatus;
import com.luemi.porcicola.mapper.ReproductiveCycleMapper;
import com.luemi.porcicola.model.Animal;
import com.luemi.porcicola.model.Farm;
import com.luemi.porcicola.model.ReproductiveCycle;
import com.luemi.porcicola.repository.AnimalRepository;
import com.luemi.porcicola.repository.FarmRepository;
import com.luemi.porcicola.repository.ReproductiveCycleRepository;
import com.luemi.porcicola.security.CurrentUser;
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
        cycle.setFarrowingNumber((int) reproductiveCycleRepository.countBySowId(sow.getId()) + 1);

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

        ReproductiveCycle cycle = reproductiveCycleMapper.toEntity(dto, existing.getSow(), existing.getFarm());
        cycle.setId(existing.getId());
        cycle.setFarrowingNumber(existing.getFarrowingNumber());

        return reproductiveCycleMapper.toDTO(reproductiveCycleRepository.save(cycle));
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
        Animal sow = animalRepository.findById(sowId)
                .orElseThrow(() -> new RuntimeException("Sow not found"));
        if (!sow.getFarm().getId().equals(farmId) || sow.getType() != AnimalType.SOW) {
            throw new RuntimeException("Sow not found");
        }
        return sow;
    }
}
