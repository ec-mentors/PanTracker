package io.everyonecodes.project_module.dtos.requests;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UsageLogRequest {
    // Will probably default to today in service layer if null
    private LocalDate useDate;

    private BigDecimal weightRecorded;

    private String notes;
}
