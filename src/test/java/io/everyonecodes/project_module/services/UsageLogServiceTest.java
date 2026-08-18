package io.everyonecodes.project_module.services;

import io.everyonecodes.project_module.dtos.requests.UsageLogRequest;
import io.everyonecodes.project_module.dtos.responses.UsageLogResponse;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageLogServiceTest {

    @Mock
    private UsageLogRepository usageLogRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectProductRepository projectProductRepository;

    @InjectMocks
    private UsageLogService usageLogService;

    @Test
    void logUsageSuccessWithoutProjectWithWeightDepletion() {
        Long productId = 10L;
        UsageLogRequest request = UsageLogRequest.builder()
                .weightRecorded(BigDecimal.valueOf(12.5))
                .notes("Daily application")
                .useDate(LocalDate.now())
                .build();

        Category category = Category.builder().id(5L).name("Foundation").build();
        Product product = Product.builder()
                .id(productId)
                .name("Luminous Silk")
                .category(category)
                .currentWeightGrams(BigDecimal.valueOf(15.0))
                .isFinished(false)
                .build();

        UsageLog savedLog = UsageLog.builder()
                .id(100L)
                .product(product)
                .useDate(request.getUseDate())
                .weightRecorded(request.getWeightRecorded())
                .notes(request.getNotes())
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(usageLogRepository.save(any(UsageLog.class))).thenReturn(savedLog);

        UsageLogResponse response = usageLogService.logUsage(productId, request);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(productId);
        assertThat(response.getWeightRecorded()).isEqualTo(BigDecimal.valueOf(12.5));
        assertThat(response.getProjectId()).isNull(); // Asserts that project context is safely null
        assertThat(product.getCurrentWeightGrams()).isEqualTo(BigDecimal.valueOf(12.5));
        assertThat(product.isFinished()).isFalse();

        verify(productRepository, times(1)).save(product);
        verify(usageLogRepository, times(1)).save(any(UsageLog.class));
    }

    @Test
    void logUsageSuccessWithProjectAndWeightHitsZeroAutoFinishesProduct() {
        Long productId = 10L;
        Long projectId = 1L;

        UsageLogRequest request = UsageLogRequest.builder()
                .projectId(projectId)
                .weightRecorded(BigDecimal.ZERO) // Weight hits zero
                .useDate(LocalDate.now())
                .build();

        Project project = Project.builder().id(projectId).name("Pan 2026").build();
        Category category = Category.builder().id(5L).name("Lipstick").build();
        Product product = Product.builder()
                .id(productId)
                .name("Red Lipstick")
                .category(category)
                .currentWeightGrams(BigDecimal.valueOf(2.0))
                .isFinished(false)
                .build();

        ProjectProductId junctionId = new ProjectProductId(projectId, productId);
        ProjectProduct junction = ProjectProduct.builder()
                .id(junctionId)
                .project(project)
                .product(product)
                .build();

        UsageLog savedLog = UsageLog.builder()
                .id(100L)
                .product(product)
                .project(project)
                .useDate(request.getUseDate())
                .weightRecorded(BigDecimal.ZERO)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectProductRepository.existsById(junctionId)).thenReturn(true);
        when(usageLogRepository.save(any(UsageLog.class))).thenReturn(savedLog);

        UsageLogResponse response = usageLogService.logUsage(productId, request);

        assertThat(response).isNotNull();
        assertThat(response.getProjectId()).isEqualTo(projectId);
        assertThat(product.isFinished()).isTrue(); // Verifies weight hitting 0 auto-completes item

        verify(productRepository, times(1)).save(product);
    }

    @Test
    void logUsage_ThrowsException_WhenProductNotFound() {
        Long productId = 99L;
        UsageLogRequest request = UsageLogRequest.builder().build();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usageLogService.logUsage(productId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void logUsageThrowsExceptionWhenProductAlreadyFinished() {
        Long productId = 10L;
        UsageLogRequest request = UsageLogRequest.builder().build();
        Product product = Product.builder().id(productId).isFinished(true).build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> usageLogService.logUsage(productId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already finished");
    }

    @Test
    void logUsageThrowsExceptionWhenWeightRecordedIsHeavierThanLast() {
        Long productId = 10L;
        UsageLogRequest request = UsageLogRequest.builder()
                .weightRecorded(BigDecimal.valueOf(20.0)) // Heavier than current 15g
                .build();

        Product product = Product.builder()
                .id(productId)
                .currentWeightGrams(BigDecimal.valueOf(15.0))
                .isFinished(false)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> usageLogService.logUsage(productId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be greater than");
    }

    @Test
    void logUsageThrowsExceptionWhenProductNotAssociatedWithProject() {
        Long productId = 10L;
        Long projectId = 1L;
        UsageLogRequest request = UsageLogRequest.builder().projectId(projectId).build();

        Project project = Project.builder().id(projectId).build();
        Product product = Product.builder().id(productId).isFinished(false).build();
        ProjectProductId junctionId = new ProjectProductId(projectId, productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectProductRepository.existsById(junctionId)).thenReturn(false);

        assertThatThrownBy(() -> usageLogService.logUsage(productId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not participating in Project");
    }

    @Test
    void getProductUsageHistorySuccess() {
        Long productId = 10L;
        Product product = Product.builder().id(productId).name("Product").build();
        UsageLog log = UsageLog.builder().id(100L).product(product).useDate(LocalDate.now()).build();

        when(productRepository.existsById(productId)).thenReturn(true);
        when(usageLogRepository.findByProductIdOrderByUseDateDesc(productId)).thenReturn(List.of(log));

        List<UsageLogResponse> responseList = usageLogService.getProductUsageHistory(productId);

        assertThat(responseList).hasSize(1);
        assertThat(responseList.getFirst().getId()).isEqualTo(100L);
    }

    @Test
    void getProductUsageHistoryThrowsExceptionWhenProductNotFound() {
        Long productId = 99L;
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThatThrownBy(() -> usageLogService.getProductUsageHistory(productId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUsageLogSuccess() {
        Long logId = 100L;
        UsageLog log = UsageLog.builder()
                .id(logId)
                .product(Product.builder().id(10L).build())
                .build();

        when(usageLogRepository.findById(logId)).thenReturn(Optional.of(log));

        usageLogService.deleteUsageLog(logId);

        verify(usageLogRepository, times(1)).delete(log);
    }

    @Test
    void deleteUsageLogThrowsExceptionWhenLogNotFound() {
        Long logId = 99L;
        when(usageLogRepository.findById(logId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usageLogService.deleteUsageLog(logId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usageLogRepository, never()).delete(any(UsageLog.class));
    }
}
