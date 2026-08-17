package io.everyonecodes.project_module.dtos.requests;

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
public class UsageLogRequest {
    // Will probably default to today in service layer if null
    private Long projectId;
    private LocalDate useDate;

    private BigDecimal weightRecorded;

    private String notes;
}
