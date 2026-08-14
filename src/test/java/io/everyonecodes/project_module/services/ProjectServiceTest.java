package io.everyonecodes.project_module.services;

import io.everyonecodes.project_module.dtos.requests.ProjectRequest;
import io.everyonecodes.project_module.dtos.responses.ProjectResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.models.Project;
import io.everyonecodes.project_module.models.User;
import io.everyonecodes.project_module.repositories.ProjectProductRepository;
import io.everyonecodes.project_module.repositories.ProjectRepository;
import io.everyonecodes.project_module.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectProductRepository projectProductRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProjectSuccess() {
        Long userId = 1L;
        User user = User.builder().id(userId).username("Stefan").build();
        ProjectRequest request = new ProjectRequest("Summer Pan", "Description", LocalDate.now().plusMonths(3));

        Project savedProject = Project.builder()
                .id(1L)
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .endDate(request.getEndDate())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        when(projectProductRepository.countByProjectId(1L)).thenReturn(0L);

        ProjectResponse response = projectService.createProject(userId, request);

        assertThat(response.getName()).isEqualTo("Summer Pan");
        assertThat(response.getProductCount()).isEqualTo(0);
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void getProjectByIdSuccess() {
        Long projectId = 10L;
        Project project = Project.builder().id(projectId).name("Test Project").user(new User()).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectProductRepository.countByProjectId(projectId)).thenReturn(3L);

        ProjectResponse response = projectService.getProjectById(projectId);

        assertThat(response.getName()).isEqualTo("Test Project");
        assertThat(response.getProductCount()).isEqualTo(3);
    }

    @Test
    void getProjectsByUserSuccess() {
        Long userId = 1L;
        Project project = Project.builder().id(1L).name("P1").user(new User()).build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(projectRepository.findByUserId(userId)).thenReturn(List.of(project));
        when(projectProductRepository.countByProjectId(1L)).thenReturn(2L);

        List<ProjectResponse> response = projectService.getProjectsByUser(userId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getProductCount()).isEqualTo(2);
    }

    @Test
    void updateProjectSuccess() {
        Long projectId = 1L;
        Project existing = Project.builder().id(projectId).name("Old Name").user(new User()).build();
        ProjectRequest request = new ProjectRequest("New Name", "New Desc", null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(existing));
        when(projectRepository.save(any(Project.class))).thenReturn(existing);
        when(projectProductRepository.countByProjectId(projectId)).thenReturn(0L);

        ProjectResponse response = projectService.updateProject(projectId, request);

        assertThat(response.getName()).isEqualTo("New Name");
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void deleteProjectSuccess() {
        Long projectId = 1L;
        Project project = Project.builder().id(projectId).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        projectService.deleteProject(projectId);

        verify(projectRepository).delete(project);
    }

    @Test
    void deleteProjectNotFound_ThrowsException() {
        Long projectId = 99L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(projectId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
