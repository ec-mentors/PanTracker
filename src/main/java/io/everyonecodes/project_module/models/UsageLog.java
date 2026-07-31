package io.everyonecodes.project_module.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "usage_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "use_date", nullable = false)
    @Builder.Default
    private LocalDate useDate = LocalDate.now();

    @Column(name = "weight_recorded", precision = 5, scale = 2)
    private BigDecimal weightRecorded;

    @Column(columnDefinition = "TEXT")
    private String notes;
}