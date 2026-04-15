package com.luemi.porcicola.controller;

import com.luemi.porcicola.dto.AuthRequest;
import com.luemi.porcicola.dto.AuthResponse;
import com.luemi.porcicola.dto.RegistroRequest;
import com.luemi.porcicola.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*") //Habilita peticiones desde cualquier Fronten
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/resgistro")
    public ResponseEntity<AuthResponse> registro(@RequestBody RegistroRequest request) {

        return ResponseEntity.ok(authService.registro(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
