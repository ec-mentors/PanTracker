package io.everyonecodes.project_module.services;

import io.everyonecodes.project_module.dtos.requests.UsageLogRequest;
import io.everyonecodes.project_module.dtos.responses.UsageLogResponse;
import io.everyonecodes.project_module.exceptions.ResourceNotFoundException;
import io.everyonecodes.project_module.models.*;
import io.everyonecodes.project_module.repositories.ProductRepository;
import io.everyonecodes.project_module.repositories.ProjectProductRepository;
import io.everyonecodes.project_module.repositories.ProjectRepository;
import io.everyonecodes.project_module.repositories.UsageLogRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsageLogService {

    private final UsageLogRepository usageLogRepository;
    private final ProductRepository productRepository;
    private final ProjectRepository projectRepository;
    private final ProjectProductRepository projectProductRepository;

    // logs single usage and updates product weight and progress
    @Transactional
    public UsageLogResponse logUsage(Long productId, UsageLogRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + productId + " not found."));

        if (product.isFinished()) {
            throw new IllegalArgumentException("Cannot log usage for an item that is already finished.");
        }

        Project project = null;
        ProjectProduct projectProduct = null;

        // synchronize Project Progress if in project
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project with ID " + request.getProjectId() + " not found."));

            ProjectProductId junctionId = new ProjectProductId(request.getProjectId(), productId);
            projectProduct = projectProductRepository.findById(junctionId)
                    .orElseThrow(() -> new IllegalArgumentException("Product with ID " + productId +
                            " is not participating in Project with ID " + request.getProjectId()));

            projectProduct.setCurrentUses(projectProduct.getCurrentUses() + 1);

            if ("USE_X_TIMES".equals(projectProduct.getGoalType()) &&
                    projectProduct.getCurrentUses().equals(projectProduct.getTargetUses())) {
                product.setFinished(true);
            }
        }

        // check if weight is correct
        if (request.getWeightRecorded() != null) {
            if (product.getCurrentWeightGrams() != null &&
                    request.getWeightRecorded().compareTo(product.getCurrentWeightGrams()) > 0) {
                throw new IllegalArgumentException("Recorded weight (" + request.getWeightRecorded() +
                        "g) cannot be greater than the product's last recorded weight (" + product.getCurrentWeightGrams() + "g).");
            }

            product.setCurrentWeightGrams(request.getWeightRecorded());

            if (product.getCurrentWeightGrams().compareTo(BigDecimal.ZERO) <= 0) {
                product.setFinished(true);
            }
        }

        // save Usagelog
        UsageLog log = UsageLog.builder()
                .product(product)
                .project(project)
                .useDate(request.getUseDate() != null ? request.getUseDate() : LocalDate.now())
                .weightRecorded(request.getWeightRecorded())
                .notes(request.getNotes())
                .build();

        UsageLog savedLog = usageLogRepository.save(log);

        // update entities
        productRepository.save(product);
        if (projectProduct != null) {
            projectProductRepository.save(projectProduct);
        }

        return mapToResponse(savedLog);
    }

    // get usage history of a product
    @Transactional(readOnly = true)
    public List<UsageLogResponse> getProductUsageHistory(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product with ID " + productId + " not found.");
        }

        return usageLogRepository.findByProductIdOrderByUseDateDesc(productId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // delete usage log and reset product
    @Transactional
    public void deleteUsageLog(Long logId) {
        UsageLog log = usageLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Usage log with ID " + logId + " not found."));

        Product product = log.getProduct();

        // if this log was associated with a project, reduce the usage count
        if (log.getProject() != null) {
            ProjectProductId junctionId = new ProjectProductId(log.getProject().getId(), product.getId());
            Optional<ProjectProduct> projectProductOpt = projectProductRepository.findById(junctionId);

            if (projectProductOpt.isPresent()) {
                ProjectProduct projectProduct = projectProductOpt.get();
                if (projectProduct.getCurrentUses() > 0) {
                    projectProduct.setCurrentUses(projectProduct.getCurrentUses() - 1);

                    // revert finished status if needed
                    if ("USE_X_TIMES".equals(projectProduct.getGoalType()) &&
                            projectProduct.getCurrentUses() < projectProduct.getTargetUses()) {
                        product.setFinished(false);
                    }
                    projectProductRepository.save(projectProduct);
                    productRepository.save(product);
                }
            }
        }

        usageLogRepository.delete(log);
    }

    private UsageLogResponse mapToResponse(UsageLog log) {
        return UsageLogResponse.builder()
                .id(log.getId())
                .productId(log.getProduct().getId())
                .productName(log.getProduct().getName())
                .projectId(log.getProject() != null ? log.getProject().getId() : null)
                .projectName(log.getProject() != null ? log.getProject().getName() : null)
                .useDate(log.getUseDate())
                .weightRecorded(log.getWeightRecorded())
                .notes(log.getNotes())
                .build();
    }
}
