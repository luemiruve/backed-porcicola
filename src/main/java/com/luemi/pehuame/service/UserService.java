package com.luemi.pehuame.service;

import com.luemi.pehuame.dto.RegisterWorkerRequest;
import com.luemi.pehuame.dto.UserDTO;
import com.luemi.pehuame.enums.UserRole;
import com.luemi.pehuame.mapper.UserMapper;
import com.luemi.pehuame.model.Farm;
import com.luemi.pehuame.model.User;
import com.luemi.pehuame.repository.FarmRepository;
import com.luemi.pehuame.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    public List<UserDTO> listByFarm(Integer farmId) {
        return userRepository.findByFarmId(farmId)
                .stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toDTO(user);
    }

    @Transactional
    public UserDTO createWorker(Integer farmId, RegisterWorkerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setFarm(farm);
        user.setRole(UserRole.WORKER);
        user.setActive(true);

        return userMapper.toDTO(userRepository.save(user));
    }

    @Transactional
    public UserDTO update(Integer userId, UserDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(dto.getName());
        user.setPhone(dto.getPhone());

        return userMapper.toDTO(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Integer userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deactivate(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }
}
