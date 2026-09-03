package com.luemi.porcicola.service;

import com.luemi.porcicola.dto.AuthRequest;
import com.luemi.porcicola.dto.AuthResponse;
import com.luemi.porcicola.dto.RegisterRequest;
import com.luemi.porcicola.enums.UserRole;
import com.luemi.porcicola.model.Farm;
import com.luemi.porcicola.model.User;
import com.luemi.porcicola.repository.FarmRepository;
import com.luemi.porcicola.repository.UserRepository;
import com.luemi.porcicola.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // Create farm first because user needs a reference to it
        Farm farm = new Farm();
        farm.setName(request.getFarmName());
        farm.setLocation(request.getFarmLocation());
        farm = farmRepository.save(farm);

        // Create admin user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setFarm(farm);
        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        // Generate token
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                farm.getId());

        return new AuthResponse(token, user.getRole().name(), farm.getId());
    }

    public AuthResponse login(AuthRequest request) {
        // Authenticate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getEmail()));

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getFarm().getId()
        );

        return new AuthResponse(token, user.getRole().name(), user.getFarm().getId());
    }

}
