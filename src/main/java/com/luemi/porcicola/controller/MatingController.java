package com.luemi.porcicola.controller;

import com.luemi.porcicola.dto.ApiResponse;
import com.luemi.porcicola.dto.MatingDTO;
import com.luemi.porcicola.service.MatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matings")
public class MatingController {

    @Autowired
    private MatingService matingService;

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @PostMapping
    public ResponseEntity<ApiResponse<MatingDTO>> create(@RequestBody MatingDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(matingService.create(dto)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MatingDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(matingService.getById(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MatingDTO>>> list(@RequestParam(required = false) Integer cycleId) {
        return ResponseEntity.ok(new ApiResponse<>(matingService.list(cycleId)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MatingDTO>> update(
            @PathVariable Integer id,
            @RequestBody MatingDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(matingService.update(id, dto)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        matingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
