package io.everyonecodes.project_module.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProductResponse {
    // Project context
    private Long projectId;
    private String projectName;

    // Product details
    private Long productId;
    private String productName;
    private String productBrand;
    private String categoryName;
    private BigDecimal currentWeightGrams;
    private boolean isFinished;

    // Challenge goals
    private String goalType;
    private Integer targetUses;
    private Integer currentUses;
}
