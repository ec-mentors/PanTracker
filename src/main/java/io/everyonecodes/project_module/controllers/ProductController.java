package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.ProductRequest;
import io.everyonecodes.project_module.dtos.responses.ProductResponse;
import io.everyonecodes.project_module.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/users/{userId}/products")
    public ResponseEntity<ProductResponse> createProduct(
            @PathVariable Long userId,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/users/{userId}/products/active")
    public ResponseEntity<List<ProductResponse>> getActiveCollection(@PathVariable Long userId) {
        List<ProductResponse> responses = productService.getActiveCollection(userId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/users/{userId}/products/empties")
    public ResponseEntity<List<ProductResponse>> getEmptyCollection(@PathVariable Long userId) {
        List<ProductResponse> responses = productService.getEmptiesCollection(userId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable Long productId) {
        ProductResponse response = productService.getProductById(productId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> updateProduct (@PathVariable Long productId, @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.updateProduct(productId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct (@PathVariable Long productId) {
        productService.deleteProduct(productId);

        return ResponseEntity.noContent().build();
    }
}
