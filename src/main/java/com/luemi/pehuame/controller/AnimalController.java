package com.luemi.pehuame.controller;

import com.luemi.pehuame.dto.AnimalDTO;
import com.luemi.pehuame.dto.ApiResponse;
import com.luemi.pehuame.enums.AnimalStatus;
import com.luemi.pehuame.enums.AnimalType;
import com.luemi.pehuame.service.AnimalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/animals")
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @PostMapping
    public ResponseEntity<ApiResponse<AnimalDTO>> create(@RequestBody AnimalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(animalService.create(dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnimalDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(animalService.getById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AnimalDTO>>> list(
            @RequestParam(required = false) AnimalType type,
            @RequestParam(required = false) AnimalStatus status) {
        return ResponseEntity.ok(new ApiResponse<>(animalService.list(type, status)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AnimalDTO>> update(
            @PathVariable Integer id,
            @RequestBody AnimalDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(animalService.update(id, dto)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        animalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
