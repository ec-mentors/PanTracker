package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.UsageLogRequest;
import io.everyonecodes.project_module.dtos.responses.UsageLogResponse;
import io.everyonecodes.project_module.services.UsageLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsageLogController.class)
class UsageLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsageLogService usageLogService;

    @Test
    void logUsage_Success_Returns201() throws Exception {
        Long productId = 10L;
        Long projectId = 1L;

        UsageLogRequest request = UsageLogRequest.builder()
                .projectId(projectId)
                .useDate(LocalDate.of(2026, 8, 28))
                .weightRecorded(BigDecimal.valueOf(29.45))
                .notes("Daily application")
                .build();

        UsageLogResponse response = UsageLogResponse.builder()
                .id(100L)
                .productId(productId)
                .productName("Luminous Silk Foundation")
                .projectId(projectId)
                .projectName("Summer Glow")
                .useDate(LocalDate.of(2026, 8, 28))
                .weightRecorded(BigDecimal.valueOf(29.45))
                .notes("Daily application")
                .build();

        when(usageLogService.logUsage(eq(productId), any(UsageLogRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products/{productId}/logs", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productName").value("Luminous Silk Foundation"))
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.weightRecorded").value(29.45))
                .andExpect(jsonPath("$.notes").value("Daily application"));
    }

    @Test
    void getProductUsageHistory_Success_Returns200() throws Exception {
        Long productId = 10L;

        UsageLogResponse responseItem = UsageLogResponse.builder()
                .id(100L)
                .productId(productId)
                .productName("Luminous Silk Foundation")
                .useDate(LocalDate.of(2026, 8, 28))
                .weightRecorded(BigDecimal.valueOf(29.45))
                .notes("Daily application")
                .build();

        when(usageLogService.getProductUsageHistory(productId)).thenReturn(List.of(responseItem));

        mockMvc.perform(get("/api/products/{productId}/logs", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].productId").value(productId))
                .andExpect(jsonPath("$[0].weightRecorded").value(29.45));
    }

    @Test
    void deleteUsageLog_Success_Returns204() throws Exception {
        Long logId = 100L;

        mockMvc.perform(delete("/api/logs/{logId}", logId))
                .andExpect(status().isNoContent());

        verify(usageLogService, times(1)).deleteUsageLog(logId);
    }
}