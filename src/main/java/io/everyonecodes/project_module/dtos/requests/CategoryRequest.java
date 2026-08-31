package io.everyonecodes.project_module.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 50, message = "Category name cannot exceed 50 characters")
    private String name;
}