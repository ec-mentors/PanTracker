package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.ProjectProductLinkRequest;
import io.everyonecodes.project_module.dtos.responses.ProjectProductResponse;
import io.everyonecodes.project_module.services.ProjectProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/products")
@RequiredArgsConstructor
public class ProjectProductController {
    private final ProjectProductService projectProductService;

    @PostMapping("/{productId}")
    public ResponseEntity<ProjectProductResponse> addProductToProject (@PathVariable Long projectId, @PathVariable Long productId, @Valid @RequestBody ProjectProductLinkRequest request) {
        ProjectProductResponse response = projectProductService.addProductToProject(projectId, productId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeProductFromProject (@PathVariable Long projectId, @PathVariable Long productId) {
        projectProductService.removeProductFromProject(projectId, productId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ProjectProductResponse>> getProductsInProject (@PathVariable Long projectId) {
        List<ProjectProductResponse> responses = projectProductService.getProductsInProject(projectId);

        return ResponseEntity.ok(responses);
    }
}
