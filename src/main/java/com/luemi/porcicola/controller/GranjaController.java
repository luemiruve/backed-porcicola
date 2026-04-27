package com.luemi.porcicola.controller;

import com.luemi.porcicola.dto.ApiResponse;
import com.luemi.porcicola.dto.GranjaDTO;
import com.luemi.porcicola.model.Granja;
import com.luemi.porcicola.service.GranjaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

// controller/GranjaController.java
@RestController
@RequestMapping("/granjas")
public class GranjaController {

    @Autowired
    private GranjaService granjaService;

    @GetMapping("/{idGranja}")
    public ResponseEntity<ApiResponse<Granja>> obtener(@PathVariable Integer idGranja) {
        Granja granja = granjaService.obtenerPorId(idGranja);
        // Aquí está la magia del estándar: envolver en ApiResponse
        return ResponseEntity.ok(new ApiResponse<>(granja));
    }

    @PutMapping("/{idGranja}")
    public ResponseEntity<ApiResponse<Granja>> actualizar(
            @PathVariable Integer idGranja,
            @RequestBody GranjaDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(granjaService.actualizar(idGranja, dto)));
    }
}