package com.luemi.porcicola.mapper;

import com.luemi.porcicola.dto.UserDTO;
import com.luemi.porcicola.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole().name());
        dto.setActive(user.getActive());
        dto.setFarmId(user.getFarm() != null ? user.getFarm().getId() : null);
        return dto;
    }
}

