package com.luemi.porcicola.service;

import com.luemi.porcicola.dto.GranjaDTO;
import com.luemi.porcicola.model.Granja;
import com.luemi.porcicola.repository.GranjaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// service/GranjaService.java
@Service
public class GranjaService {

    @Autowired
    private GranjaRepository granjaRepository;

    public Granja obtenerPorId(Integer idGranja) {
        return granjaRepository.findById(idGranja)
                .orElseThrow(() -> new RuntimeException("Granja no encontrada"));
    }

    public Granja actualizar(Integer idGranja, GranjaDTO dto) {
        Granja granja = obtenerPorId(idGranja);
        granja.setNombre(dto.getNombre());
        granja.setUbicacion(dto.getUbicacion());
        return granjaRepository.save(granja);
    }
}