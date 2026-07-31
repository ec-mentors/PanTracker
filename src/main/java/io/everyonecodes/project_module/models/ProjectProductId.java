package io.everyonecodes.project_module.models;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectProductId implements Serializable {

    private Long projectId;
    private Long productId;
}
