package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.ProjectRequest;
import io.everyonecodes.project_module.dtos.responses.ProjectResponse;
import io.everyonecodes.project_module.services.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/user/{userId}/projects")
    private ResponseEntity<ProjectResponse> createProject (@PathVariable Long userId, @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}/projects")
    private ResponseEntity<List<ProjectResponse>> getProjectsByUser (@PathVariable Long userId) {
        List<ProjectResponse> responses = projectService.getProjectsByUser(userId);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/projects/{projectId}")
    private ResponseEntity<ProjectResponse> getProjectById (@PathVariable Long projectId) {
        ProjectResponse response = projectService.getProjectById(projectId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/projects/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId, @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.updateProject(projectId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId) {
        projectService.deleteProject(projectId);

        return ResponseEntity.noContent().build();
    }
}
