package io.everyonecodes.project_module.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;
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
//when someone adds a product, the following data is sent.
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String brand;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Opening date is required")
    private LocalDate openingDate;

    @NotNull(message = "Period after opening (PAO) is required")
    @PositiveOrZero(message = "PAO cannot be negative")
    private Integer periodAfterOpeningMonths;

    private BigDecimal startingWeightGrams;
    private LocalDate purchaseDate;
}
