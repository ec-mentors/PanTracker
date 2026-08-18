package io.everyonecodes.project_module.services;

import io.everyonecodes.project_module.dtos.requests.ProjectProductLinkRequest;
import io.everyonecodes.project_module.dtos.responses.ProjectProductResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.models.*;
import io.everyonecodes.project_module.repositories.ProductRepository;
import io.everyonecodes.project_module.repositories.ProjectProductRepository;
import io.everyonecodes.project_module.repositories.ProjectRepository;
import io.everyonecodes.project_module.repositories.UsageLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectProductServiceTest {

    @Mock
    private ProjectProductRepository projectProductRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UsageLogRepository usageLogRepository;

    @InjectMocks
    private ProjectProductService projectProductService;

    //Adding products to projects
    @Test
    void addProductToProjectSuccess() {
        Long projectId = 1L;
        Long productId = 10L;

        ProjectProductLinkRequest request = ProjectProductLinkRequest.builder()
                .goalType("HIT_PAN")
                .targetUses(30)
                .build();

        Project project = Project.builder().id(projectId).name("Summer Pan Project").build();
        Category category = Category.builder().id(5L).name("Eyeshadow").build();
        Product product = Product.builder().id(productId).name("Bronze Eyeshadow").category(category).build();
        ProjectProductId compositeId = new ProjectProductId(projectId, productId);

        ProjectProduct savedJunction = ProjectProduct.builder()
                .id(compositeId)
                .project(project)
                .product(product)
                .goalType("HIT_PAN")
                .targetUses(30)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(projectProductRepository.existsById(compositeId)).thenReturn(false);
        when(projectProductRepository.save(any(ProjectProduct.class))).thenReturn(savedJunction);
        when(usageLogRepository.countByProductIdAndProjectId(productId, projectId)).thenReturn(0);

        ProjectProductResponse response = projectProductService.addProductToProject(projectId, productId, request);

        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isEqualTo(projectId);
        assertThat(response.getProductId()).isEqualTo(productId);
        assertThat(response.getGoalType()).isEqualTo("HIT_PAN");

        verify(projectProductRepository, times(1)).save(any(ProjectProduct.class));
    }

    @Test
    void addProductToProjectThrowsExceptionWhenProjectNotFound() {
        Long projectId = 5L;
        Long productId = 15L;
        ProjectProductLinkRequest request = ProjectProductLinkRequest.builder().build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectProductService.addProductToProject(projectId, productId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project with ID 5 not found");

        verify(projectProductRepository, never()).save(any(ProjectProduct.class));
    }

    @Test
    void addProductToProjectThrowsExceptionWhenProductNotFound() {
        Long projectId = 7L;
        Long productId = 17L;
        ProjectProductLinkRequest request = ProjectProductLinkRequest.builder().build();
        Project project = Project.builder().id(projectId).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectProductService.addProductToProject(projectId, productId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product with ID 17 not found");

        verify(projectProductRepository, never()).save(any(ProjectProduct.class));
    }

    @Test
    void addProductToProjectThrowsExceptionWhenLinkAlreadyExists() {
        Long projectId = 3L;
        Long productId = 13L;
        ProjectProductLinkRequest request = ProjectProductLinkRequest.builder().build();

        Project project = Project.builder().id(projectId).build();
        Product product = Product.builder().id(productId).build();
        ProjectProductId compositeId = new ProjectProductId(projectId, productId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(projectProductRepository.existsById(compositeId)).thenReturn(true);

        assertThatThrownBy(() -> projectProductService.addProductToProject(projectId, productId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already linked to this project");

        verify(projectProductRepository, never()).save(any(ProjectProduct.class));
    }

    //removing product from project
    @Test
    void removeProductFromProjectSuccess() {
        Long projectId = 7L;
        Long productId = 17L;
        ProjectProductId compositeId = new ProjectProductId(projectId, productId);
        ProjectProduct link = ProjectProduct.builder().id(compositeId).build();

        when(projectProductRepository.findById(compositeId)).thenReturn(Optional.of(link));

        projectProductService.removeProductFromProject(projectId, productId);

        verify(projectProductRepository, times(1)).delete(link);
    }

    @Test
    void removeProductFromProjectThrowsExceptionWhenLinkNotFound() {
        Long projectId = 5L;
        Long productId = 72L;
        ProjectProductId compositeId = new ProjectProductId(projectId, productId);

        when(projectProductRepository.findById(compositeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectProductService.removeProductFromProject(projectId, productId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Link between Project 5 and Product 72 not found");

        verify(projectProductRepository, never()).delete(any(ProjectProduct.class));
    }

    //get product from project
    @Test
    void getProductsInProjectSuccess() {
        Long projectId = 1L;
        Project project = Project.builder().id(projectId).name("Project A").build();
        Category category = Category.builder().id(5L).name("Mascara").build();
        Product product = Product.builder().id(10L).name("Ultra Black Mascara").category(category).build();

        ProjectProduct link = ProjectProduct.builder()
                .id(new ProjectProductId(projectId, 10L))
                .project(project)
                .product(product)
                .goalType("HIT_PAN")
                .targetUses(20)
                .build();

        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(projectProductRepository.findByProjectId(projectId)).thenReturn(List.of(link));
        when(usageLogRepository.countByProductIdAndProjectId(10L, projectId)).thenReturn(5);

        List<ProjectProductResponse> responseList = projectProductService.getProductsInProject(projectId);

        assertThat(responseList).isNotEmpty().hasSize(1);
        ProjectProductResponse response = responseList.getFirst();
        assertThat(response.getProjectId()).isEqualTo(projectId);
        assertThat(response.getProductId()).isEqualTo(10L);
        assertThat(response.getCategoryName()).isEqualTo("Mascara");
    }

    @Test
    void getProductsInProjectThrowsExceptionWhenProjectNotFound() {
        Long projectId = 32L;
        when(projectRepository.existsById(projectId)).thenReturn(false);

        assertThatThrownBy(() -> projectProductService.getProductsInProject(projectId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project with ID 32 not found");

        verify(projectProductRepository, never()).findByProjectId(anyLong());
    }
}