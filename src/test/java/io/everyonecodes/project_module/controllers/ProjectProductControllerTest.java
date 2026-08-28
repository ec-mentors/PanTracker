package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.ProjectProductLinkRequest;
import io.everyonecodes.project_module.dtos.responses.ProjectProductResponse;
import io.everyonecodes.project_module.services.ProjectProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectProductController.class)
class ProjectProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectProductService projectProductService;

    @Test
    void addProductToProjectSuccessReturns201() throws Exception {
        Long projectId = 1L;
        Long productId = 10L;

        ProjectProductLinkRequest request = ProjectProductLinkRequest.builder()
                .goalType("USE_X_TIMES")
                .targetUses(30)
                .build();

        ProjectProductResponse response = ProjectProductResponse.builder()
                .projectId(projectId)
                .projectName("Summer Pan")
                .productId(productId)
                .productName("Bronze Eyeshadow")
                .productBrand("NARS")
                .categoryName("Eyeshadow")
                .currentWeightGrams(BigDecimal.valueOf(15.5))
                .isFinished(false)
                .goalType("USE_X_TIMES")
                .targetUses(30)
                .currentUses(0)
                .build();

        when(projectProductService.addProductToProject(eq(projectId), eq(productId), any(ProjectProductLinkRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/projects/{projectId}/products/{productId}", projectId, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.goalType").value("USE_X_TIMES"))
                .andExpect(jsonPath("$.targetUses").value(30))
                .andExpect(jsonPath("$.finished").value(false));
    }

    @Test
    void addProductToProjectFailureInvalidRequestReturns400() throws Exception {
        Long projectId = 1L;
        Long productId = 10L;

        ProjectProductLinkRequest request = ProjectProductLinkRequest.builder()
                .goalType("USE_X_TIMES")
                .targetUses(-5)
                .build();

        mockMvc.perform(post("/api/projects/{projectId}/products/{productId}", projectId, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.targetUses").value("Target uses must be greater than 0"));
    }

    @Test
    void getProductsInProjectSuccessReturns200() throws Exception {
        Long projectId = 1L;

        ProjectProductResponse responseItem = ProjectProductResponse.builder()
                .projectId(projectId)
                .productId(10L)
                .productName("Bronze Eyeshadow")
                .categoryName("Eyeshadow")
                .currentWeightGrams(BigDecimal.valueOf(15.5))
                .isFinished(false)
                .goalType("USE_X_TIMES")
                .targetUses(30)
                .currentUses(3)
                .build();

        when(projectProductService.getProductsInProject(projectId)).thenReturn(List.of(responseItem));

        mockMvc.perform(get("/api/projects/{projectId}/products", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value(projectId))
                .andExpect(jsonPath("$[0].productId").value(10L))
                .andExpect(jsonPath("$[0].currentUses").value(3));
    }

    @Test
    void removeProductFromProjectSuccessReturns204() throws Exception {
        Long projectId = 1L;
        Long productId = 10L;

        mockMvc.perform(delete("/api/projects/{projectId}/products/{productId}", projectId, productId))
                .andExpect(status().isNoContent());

        verify(projectProductService, times(1)).removeProductFromProject(projectId, productId);
    }
}