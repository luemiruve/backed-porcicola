package com.luemi.pehuame.service;

import com.luemi.pehuame.dto.MatingDTO;
import com.luemi.pehuame.enums.AnimalType;
import com.luemi.pehuame.enums.CycleStatus;
import com.luemi.pehuame.mapper.MatingMapper;
import com.luemi.pehuame.model.Animal;
import com.luemi.pehuame.model.Mating;
import com.luemi.pehuame.model.ReproductiveCycle;
import com.luemi.pehuame.repository.AnimalRepository;
import com.luemi.pehuame.repository.MatingRepository;
import com.luemi.pehuame.repository.ReproductiveCycleRepository;
import com.luemi.pehuame.security.CurrentUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatingService {

    @Autowired
    private MatingRepository matingRepository;

    @Autowired
    private ReproductiveCycleRepository reproductiveCycleRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private MatingMapper matingMapper;

    @Transactional
    public MatingDTO create(MatingDTO dto) {
        Integer farmId = CurrentUser.get().getFarmId();
        ReproductiveCycle cycle = resolveCycleForCreate(dto.getCycleId(), farmId);
        Animal boar = resolveBoar(dto.getBoarId(), farmId);

        Mating mating = matingMapper.toEntity(dto, cycle, boar);
        Mating saved = matingRepository.save(mating);

        recalculateExpectedFarrowingDate(cycle.getId());

        return matingMapper.toDTO(saved);
    }

    public MatingDTO getById(Integer id) {
        return matingMapper.toDTO(getOwnedMating(id));
    }

    public List<MatingDTO> list(Integer cycleId) {
        if (cycleId == null) {
            throw new RuntimeException("cycleId is required");
        }
        Integer farmId = CurrentUser.get().getFarmId();
        resolveCycle(cycleId, farmId);

        return matingRepository.findByCycleId(cycleId)
                .stream()
                .map(matingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MatingDTO update(Integer id, MatingDTO dto) {
        Mating existing = getOwnedMating(id);
        Integer farmId = CurrentUser.get().getFarmId();
        Animal boar = resolveBoar(dto.getBoarId(), farmId);

        Mating mating = matingMapper.toEntity(dto, existing.getCycle(), boar);
        mating.setId(existing.getId());

        Mating saved = matingRepository.save(mating);

        recalculateExpectedFarrowingDate(existing.getCycle().getId());

        return matingMapper.toDTO(saved);
    }

    @Transactional
    public void delete(Integer id) {
        Mating existing = getOwnedMating(id);
        Integer cycleId = existing.getCycle().getId();

        matingRepository.delete(existing);

        recalculateExpectedFarrowingDate(cycleId);
    }

    private void recalculateExpectedFarrowingDate(Integer cycleId) {
        ReproductiveCycle cycle = reproductiveCycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Reproductive cycle not found"));

        Mating mostRecent = matingRepository.findFirstByCycleIdOrderByMatingDateDesc(cycleId)
                .orElse(null);

        if (mostRecent != null) {
            LocalDate expectedFarrowingDate = mostRecent.getMatingDate().toLocalDate().plusDays(114);
            cycle.setExpectedFarrowingDate(expectedFarrowingDate);
        } else {
            cycle.setExpectedFarrowingDate(null);
        }

        reproductiveCycleRepository.save(cycle);
    }

    private Mating getOwnedMating(Integer id) {
        Mating mating = matingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mating not found"));
        if (!mating.getCycle().getFarm().getId().equals(CurrentUser.get().getFarmId())) {
            throw new RuntimeException("Mating not found");
        }
        return mating;
    }

    private ReproductiveCycle resolveCycleForCreate(Integer cycleId, Integer farmId) {
        ReproductiveCycle cycle = resolveCycle(cycleId, farmId);
        if (cycle.getStatus() != CycleStatus.GESTATION) {
            throw new RuntimeException("Reproductive cycle not found");
        }
        return cycle;
    }

    private ReproductiveCycle resolveCycle(Integer cycleId, Integer farmId) {
        if (cycleId == null) {
            throw new RuntimeException("Reproductive cycle not found");
        }
        ReproductiveCycle cycle = reproductiveCycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Reproductive cycle not found"));
        if (!cycle.getFarm().getId().equals(farmId)) {
            throw new RuntimeException("Reproductive cycle not found");
        }
        return cycle;
    }

    private Animal resolveBoar(Integer boarId, Integer farmId) {
        if (boarId == null) {
            return null;
        }
        Animal boar = animalRepository.findById(boarId)
                .orElseThrow(() -> new RuntimeException("Boar not found"));
        if (!boar.getFarm().getId().equals(farmId) || boar.getType() != AnimalType.BOAR) {
            throw new RuntimeException("Boar not found");
        }
        return boar;
    }
}
