package io.everyonecodes.project_module.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//when someone adds a product, the following data is sent.
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String brand;

    private Long categoryId;

    private String newCategoryName;

    @NotNull(message = "Opening date is required")
    private LocalDate openingDate;

    @NotNull(message = "Period after opening (PAO) is required")
    @PositiveOrZero(message = "PAO cannot be negative")
    private Integer periodAfterOpeningMonths;

    private BigDecimal startingWeightGrams;
    private LocalDate purchaseDate;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 10, message = "Rating cannot exceed 10")
    private Integer rating;

    private String notes;
    private Boolean isFinished;
}
