package io.everyonecodes.project_module.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    // Derived value computed by the service layer to satisfy your proposal
    private Integer productCount;
}
