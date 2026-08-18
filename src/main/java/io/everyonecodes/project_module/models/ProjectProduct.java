package io.everyonecodes.project_module.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectProduct {

    @EmbeddedId
    private ProjectProductId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "goal_type", nullable = false, length = 30)
    @Builder.Default
    private String goalType = "FINISH_COMPLETELY";

    @Column(name = "target_uses")
    private Integer targetUses;
}
