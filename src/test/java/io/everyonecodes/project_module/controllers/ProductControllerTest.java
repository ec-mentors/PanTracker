package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.ProductRequest;
import io.everyonecodes.project_module.dtos.responses.ProductResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.services.ProductService;
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

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void createProductSuccessReturns201() throws Exception {
        Long userId = 1L;
        ProductRequest request = ProductRequest.builder()
                .name("Velvet Lip Glide")
                .brand("NARS")
                .categoryId(3L)
                .openingDate(LocalDate.of(2026, 1, 15))
                .periodAfterOpeningMonths(12)
                .startingWeightGrams(BigDecimal.valueOf(15.5))
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(10L)
                .name("Velvet Lip Glide")
                .brand("NARS")
                .categoryName("Lipstick")
                .openingDate(LocalDate.of(2026, 1, 15))
                .periodAfterOpeningMonths(12)
                .startingWeightGrams(BigDecimal.valueOf(15.5))
                .currentWeightGrams(BigDecimal.valueOf(15.5))
                .isFinished(false)
                .totalUses(0)
                .build();

        when(productService.createProduct(eq(userId), any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/{userId}/products", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // Expects 201 Created
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Velvet Lip Glide"))
                .andExpect(jsonPath("$.categoryName").value("Lipstick"))
                .andExpect(jsonPath("$.currentWeightGrams").value(15.5))
                .andExpect(jsonPath("$.finished").value(false));
    }

    @Test
    void createProductFailureInvalidRequestReturns400() throws Exception {
        // missing name to trigger @NotBlank validation check
        Long userId = 1L;
        ProductRequest request = ProductRequest.builder()
                .name("")
                .categoryId(3L)
                .openingDate(LocalDate.of(2026, 1, 15))
                .periodAfterOpeningMonths(12)
                .build();

        mockMvc.perform(post("/api/users/{userId}/products", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // Expects 400 Bad Request
                .andExpect(jsonPath("$.name").value("Product name is required"));
    }

    @Test
    void getActiveCollectionSuccessReturns200() throws Exception {
        Long userId = 1L;
        ProductResponse activeProduct = ProductResponse.builder()
                .id(10L)
                .name("Active Item")
                .isFinished(false)
                .build();

        when(productService.getActiveCollection(userId)).thenReturn(List.of(activeProduct));

        mockMvc.perform(get("/api/users/{userId}/products/active", userId))
                .andExpect(status().isOk()) // Expects 200 OK
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].finished").value(false));
    }

    @Test
    void getEmptyCollectionSuccessReturns200() throws Exception {
        Long userId = 1L;
        ProductResponse emptyProduct = ProductResponse.builder()
                .id(11L)
                .name("Empty Item")
                .isFinished(true)
                .build();

        when(productService.getEmptiesCollection(userId)).thenReturn(List.of(emptyProduct));

        mockMvc.perform(get("/api/users/{userId}/products/empties", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11L))
                .andExpect(jsonPath("$[0].finished").value(true));
    }

    @Test
    void getProductByIdSuccessReturns200() throws Exception {
        Long productId = 10L;
        ProductResponse product = ProductResponse.builder()
                .id(productId)
                .name("Luminous Silk")
                .build();

        when(productService.getProductById(productId)).thenReturn(product);

        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Luminous Silk"));
    }

    @Test
    void getProductByIdNotFoundReturns404() throws Exception {
        Long productId = 99L;
        when(productService.getProductById(productId))
                .thenThrow(new ResourceNotFoundException("Product with ID 99 not found."));

        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isNotFound()) // Expects 404 Not Found
                .andExpect(jsonPath("$.error").value("Product with ID 99 not found."));
    }

    @Test
    void updateProductSuccessReturns200() throws Exception {
        Long productId = 10L;
        ProductRequest request = ProductRequest.builder()
                .name("Updated Name")
                .categoryId(3L)
                .openingDate(LocalDate.of(2026, 1, 15))
                .periodAfterOpeningMonths(12)
                .isFinished(true)
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(productId)
                .name("Updated Name")
                .isFinished(true)
                .build();

        when(productService.updateProduct(eq(productId), any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Expects 200 OK
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.finished").value(true));
    }

    @Test
    void deleteProductSuccessReturns204() throws Exception {
        Long productId = 10L;

        mockMvc.perform(delete("/api/products/{productId}", productId))
                .andExpect(status().isNoContent()); // Expects 204 No Content

        verify(productService, times(1)).deleteProduct(productId);
    }
}