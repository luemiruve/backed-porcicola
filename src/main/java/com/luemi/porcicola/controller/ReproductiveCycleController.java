package com.luemi.porcicola.controller;

import com.luemi.porcicola.dto.ApiResponse;
import com.luemi.porcicola.dto.ReproductiveCycleDTO;
import com.luemi.porcicola.enums.CycleStatus;
import com.luemi.porcicola.service.ReproductiveCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reproductive-cycles")
public class ReproductiveCycleController {

    @Autowired
    private ReproductiveCycleService reproductiveCycleService;

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ReproductiveCycleDTO>> create(@RequestBody ReproductiveCycleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(reproductiveCycleService.create(dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReproductiveCycleDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(reproductiveCycleService.getById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReproductiveCycleDTO>>> list(
            @RequestParam(required = false) Integer sowId,
            @RequestParam(required = false) CycleStatus status) {
        return ResponseEntity.ok(new ApiResponse<>(reproductiveCycleService.list(sowId, status)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReproductiveCycleDTO>> update(
            @PathVariable Integer id,
            @RequestBody ReproductiveCycleDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(reproductiveCycleService.update(id, dto)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        reproductiveCycleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
