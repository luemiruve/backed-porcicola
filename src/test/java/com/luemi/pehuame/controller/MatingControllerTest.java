package com.luemi.pehuame.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luemi.pehuame.dto.MatingDTO;
import com.luemi.pehuame.service.MatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MatingControllerTest {

    @Mock
    private MatingService matingService;

    @InjectMocks
    private MatingController matingController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(matingController).build();
    }

    @Test
    void create_returns201WithCreatedMating() throws Exception {
        MatingDTO request = new MatingDTO();
        request.setCycleId(10);
        request.setMatingDate(LocalDateTime.of(2026, 1, 1, 8, 0));

        MatingDTO created = new MatingDTO();
        created.setId(1);
        created.setCycleId(10);

        when(matingService.create(any(MatingDTO.class))).thenReturn(created);

        mockMvc.perform(post("/matings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.cycleId").value(10));
    }

    @Test
    void getById_returns200WithMating() throws Exception {
        MatingDTO mating = new MatingDTO();
        mating.setId(1);
        mating.setCycleId(10);

        when(matingService.getById(1)).thenReturn(mating);

        mockMvc.perform(get("/matings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void list_passesCycleIdFilter() throws Exception {
        when(matingService.list(10)).thenReturn(List.of());

        mockMvc.perform(get("/matings").param("cycleId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void update_returns200WithUpdatedMating() throws Exception {
        MatingDTO request = new MatingDTO();
        request.setMatingDate(LocalDateTime.of(2026, 1, 5, 8, 0));

        MatingDTO updated = new MatingDTO();
        updated.setId(1);
        updated.setCycleId(10);

        when(matingService.update(eq(1), any(MatingDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/matings/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cycleId").value(10));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/matings/1"))
                .andExpect(status().isNoContent());
    }
}
