package io.everyonecodes.project_module.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;

    // user context
    private Long userId;
    private String username;

    // Category context
    private Long categoryId;
    private String categoryName;

    // Product Details
    private String name;
    private String brand;
    private LocalDate purchaseDate;
    private LocalDate openingDate;
    private Integer periodAfterOpeningMonths;
    private BigDecimal startingWeightGrams;
    private BigDecimal currentWeightGrams;
    private Integer rating;
    private boolean isFinished;
}
