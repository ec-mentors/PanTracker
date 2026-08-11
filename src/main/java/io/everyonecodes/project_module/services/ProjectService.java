package io.everyonecodes.project_module.services;

import io.everyonecodes.project_module.dtos.requests.ProjectRequest;
import io.everyonecodes.project_module.dtos.responses.ProjectResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.models.Project;
import io.everyonecodes.project_module.models.User;
import io.everyonecodes.project_module.repositories.ProjectProductRepository;
import io.everyonecodes.project_module.repositories.ProjectRepository;
import io.everyonecodes.project_module.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectProductRepository projectProductRepository;

    @Transactional
    public ProjectResponse createProject(Long userId, ProjectRequest request) {
        // check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found."));

        // map dto to entity
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .endDate(request.getEndDate())
                .build();

        // save and return response
        Project savedProject = projectRepository.save(project);
        return mapToResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with ID " + userId + " not found.");
        }

        return projectRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project with ID " + projectId + " not found."));
        return mapToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        // fetch project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project with ID " + projectId + " not found."));

        // update fields
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setEndDate(request.getEndDate());

        // save and return project
        Project updatedProject = projectRepository.save(project);
        return mapToResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project with ID " + projectId + " not found."));

        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        // Fetch only the count from the junction table rather than retrieving all entity fields
        long count = projectProductRepository.countByProjectId(project.getId());

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .productCount((int) count)
                .build();
    }
}
