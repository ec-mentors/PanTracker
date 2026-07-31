package io.everyonecodes.project_module.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class ProjectProductLinkRequest {
    @NotBlank(message = "Goal type is required (e.g. FINISH, HIT_PAN)")
    private String goalType;

    @Positive(message = "Target uses must be greater than 0")
    private Integer targetUses;
}
