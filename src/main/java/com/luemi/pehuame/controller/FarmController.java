package com.luemi.pehuame.controller;

import com.luemi.pehuame.dto.ApiResponse;
import com.luemi.pehuame.dto.FarmDTO;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.service.FarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// controller/FarmController.java
@RestController
@RequestMapping("/farms")
public class FarmController {

    @Autowired
    private FarmService farmService;

    @GetMapping("/{farmId}")
    public ResponseEntity<ApiResponse<Farm>> getById(@PathVariable Integer farmId) {
        Farm farm = farmService.getById(farmId);
        // Standard: wrap responses in ApiResponse
        return ResponseEntity.ok(new ApiResponse<>(farm));
    }

    @PutMapping("/{farmId}")
    public ResponseEntity<ApiResponse<Farm>> update(
            @PathVariable Integer farmId,
            @RequestBody FarmDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(farmService.update(farmId, dto)));
    }
}