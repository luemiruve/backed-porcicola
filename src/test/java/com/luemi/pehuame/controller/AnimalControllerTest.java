package com.luemi.pehuame.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luemi.pehuame.dto.AnimalDTO;
import com.luemi.pehuame.enums.AnimalStatus;
import com.luemi.pehuame.enums.AnimalType;
import com.luemi.pehuame.service.AnimalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnimalControllerTest {

    @Mock
    private AnimalService animalService;

    @InjectMocks
    private AnimalController animalController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(animalController).build();
    }

    @Test
    void create_returns201WithCreatedAnimal() throws Exception {
        AnimalDTO request = new AnimalDTO();
        request.setName("Bella");
        request.setType(AnimalType.SOW);

        AnimalDTO created = new AnimalDTO();
        created.setId(1);
        created.setName("Bella");
        created.setType(AnimalType.SOW);

        when(animalService.create(any(AnimalDTO.class))).thenReturn(created);

        mockMvc.perform(post("/animals")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Bella"));
    }

    @Test
    void getById_returns200WithAnimal() throws Exception {
        AnimalDTO animal = new AnimalDTO();
        animal.setId(1);
        animal.setName("Bella");

        when(animalService.getById(1)).thenReturn(animal);

        mockMvc.perform(get("/animals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void list_passesTypeAndStatusFilters() throws Exception {
        when(animalService.list(AnimalType.SOW, AnimalStatus.ACTIVE)).thenReturn(List.of());

        mockMvc.perform(get("/animals").param("type", "SOW").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void update_returns200WithUpdatedAnimal() throws Exception {
        AnimalDTO request = new AnimalDTO();
        request.setName("New name");
        request.setType(AnimalType.SOW);

        AnimalDTO updated = new AnimalDTO();
        updated.setId(1);
        updated.setName("New name");

        when(animalService.update(eq(1), any(AnimalDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/animals/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New name"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/animals/1"))
                .andExpect(status().isNoContent());
    }
}
