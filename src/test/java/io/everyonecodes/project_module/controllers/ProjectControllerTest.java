package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.ProjectRequest;
import io.everyonecodes.project_module.dtos.responses.ProjectResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void createProject_Success_Returns201() throws Exception {
        Long userId = 1L;
        ProjectRequest request = ProjectRequest.builder()
                .name("Summer Glow Project")
                .description("Using up warm-toned makeup")
                .endDate(LocalDate.of(2026, 9, 30))
                .build();

        ProjectResponse response = ProjectResponse.builder()
                .id(1L)
                .name("Summer Glow Project")
                .description("Using up warm-toned makeup")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 9, 30))
                .productCount(0)
                .build();

        when(projectService.createProject(eq(userId), any(ProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/{userId}/projects", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Summer Glow Project"))
                .andExpect(jsonPath("$.productCount").value(0));
    }

    @Test
    void createProject_Failure_InvalidRequest_Returns400() throws Exception {
        Long userId = 1L;
        ProjectRequest request = ProjectRequest.builder()
                .name("")
                .description("Invalid project")
                .build();

        mockMvc.perform(post("/api/users/{userId}/projects", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Project name cannot be blank"));
    }

    @Test
    void getProjectsByUser_Success_Returns200() throws Exception {
        Long userId = 1L;
        ProjectResponse project = ProjectResponse.builder()
                .id(1L)
                .name("Summer Glow Project")
                .productCount(3)
                .build();

        when(projectService.getProjectsByUser(userId)).thenReturn(List.of(project));

        mockMvc.perform(get("/api/users/{userId}/projects", userId))
                .andExpect(status().isOk()) // Expects 200 OK
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Summer Glow Project"))
                .andExpect(jsonPath("$[0].productCount").value(3));
    }

    @Test
    void getProjectById_Success_Returns200() throws Exception {
        Long projectId = 1L;
        ProjectResponse project = ProjectResponse.builder()
                .id(projectId)
                .name("Pan 2026 Challenge")
                .productCount(5)
                .build();

        when(projectService.getProjectById(projectId)).thenReturn(project);

        mockMvc.perform(get("/api/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Pan 2026 Challenge"))
                .andExpect(jsonPath("$.productCount").value(5));
    }

    @Test
    void getProjectById_NotFound_Returns404() throws Exception {
        Long projectId = 99L;
        when(projectService.getProjectById(projectId))
                .thenThrow(new ResourceNotFoundException("Project with ID 99 not found."));

        mockMvc.perform(get("/api/projects/{projectId}", projectId))
                .andExpect(status().isNotFound()) // Expects 404 Not Found
                .andExpect(jsonPath("$.error").value("Project with ID 99 not found."));
    }

    @Test
    void updateProject_Success_Returns200() throws Exception {
        Long projectId = 1L;
        ProjectRequest request = ProjectRequest.builder()
                .name("Updated Challenge Name")
                .description("Updated description")
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        ProjectResponse response = ProjectResponse.builder()
                .id(projectId)
                .name("Updated Challenge Name")
                .description("Updated description")
                .productCount(2)
                .build();

        when(projectService.updateProject(eq(projectId), any(ProjectRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Updated Challenge Name"))
                .andExpect(jsonPath("$.productCount").value(2));
    }

    @Test
    void deleteProject_Success_Returns204() throws Exception {
        Long projectId = 1L;

        mockMvc.perform(delete("/api/projects/{projectId}", projectId))
                .andExpect(status().isNoContent()); // Expects 204 No Content

        verify(projectService, times(1)).deleteProject(projectId);
    }
}