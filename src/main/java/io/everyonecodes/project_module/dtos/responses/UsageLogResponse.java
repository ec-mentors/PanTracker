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
public class UsageLogResponse {
    private Long id;

    // Product Context
    private Long productId;
    private String productName;

    // Project Context
    private Long projectId;
    private String projectName;

    // Log Metadata
    private LocalDate useDate;
    private BigDecimal weightRecorded;
    private String notes;
}
