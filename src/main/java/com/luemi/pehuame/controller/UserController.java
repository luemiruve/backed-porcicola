package com.luemi.pehuame.controller;

import com.luemi.pehuame.dto.ApiResponse;
import com.luemi.pehuame.dto.ChangePasswordRequest;
import com.luemi.pehuame.dto.RegisterWorkerRequest;
import com.luemi.pehuame.dto.UserDTO;
import com.luemi.pehuame.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    // List users by farm
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/farms/{farmId}/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> listByFarm(@PathVariable Integer farmId) {
        return ResponseEntity.ok(new ApiResponse<>(userService.listByFarm(farmId)));
    }

    // Get user by id
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(new ApiResponse<>(userService.getById(id)));
    }

    // Create worker in a farm
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/farms/{farmId}/users")
    public ResponseEntity<ApiResponse<UserDTO>> createWorker(
            @PathVariable Integer farmId,
            @RequestBody RegisterWorkerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(userService.createWorker(farmId, request)));
    }

    // Update user data
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> update(
            @PathVariable Integer id,
            @RequestBody UserDTO dto) {
        return ResponseEntity.ok(new ApiResponse<>(userService.update(id, dto)));
    }

    // Change password (self-service only, requires current password)
    @PreAuthorize("hasAnyRole('ADMIN','WORKER')")
    @PostMapping("/users/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Integer id,
            @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    // Deactivate user
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
