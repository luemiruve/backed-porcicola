package com.luemi.porcicola.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luemi.porcicola.dto.ReproductiveCycleDTO;
import com.luemi.porcicola.enums.CycleStatus;
import com.luemi.porcicola.service.ReproductiveCycleService;
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
class ReproductiveCycleControllerTest {

    @Mock
    private ReproductiveCycleService reproductiveCycleService;

    @InjectMocks
    private ReproductiveCycleController reproductiveCycleController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reproductiveCycleController).build();
    }

    @Test
    void create_returns201WithCreatedCycle() throws Exception {
        ReproductiveCycleDTO request = new ReproductiveCycleDTO();
        request.setSowId(5);

        ReproductiveCycleDTO created = new ReproductiveCycleDTO();
        created.setId(1);
        created.setSowId(5);
        created.setFarrowingNumber(1);

        when(reproductiveCycleService.create(any(ReproductiveCycleDTO.class))).thenReturn(created);

        mockMvc.perform(post("/reproductive-cycles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.farrowingNumber").value(1));
    }

    @Test
    void getById_returns200WithCycle() throws Exception {
        ReproductiveCycleDTO cycle = new ReproductiveCycleDTO();
        cycle.setId(1);
        cycle.setSowId(5);

        when(reproductiveCycleService.getById(1)).thenReturn(cycle);

        mockMvc.perform(get("/reproductive-cycles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void list_passesSowIdAndStatusFilters() throws Exception {
        when(reproductiveCycleService.list(5, CycleStatus.GESTATION)).thenReturn(List.of());

        mockMvc.perform(get("/reproductive-cycles").param("sowId", "5").param("status", "GESTATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void update_returns200WithUpdatedCycle() throws Exception {
        ReproductiveCycleDTO request = new ReproductiveCycleDTO();
        request.setStatus(CycleStatus.LACTATION);

        ReproductiveCycleDTO updated = new ReproductiveCycleDTO();
        updated.setId(1);
        updated.setStatus(CycleStatus.LACTATION);

        when(reproductiveCycleService.update(eq(1), any(ReproductiveCycleDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/reproductive-cycles/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LACTATION"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/reproductive-cycles/1"))
                .andExpect(status().isNoContent());
    }
}
