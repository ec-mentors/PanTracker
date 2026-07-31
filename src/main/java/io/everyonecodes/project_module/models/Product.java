package io.everyonecodes.project_module.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String brand;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "period_after_opening_months", nullable = false)
    private Integer periodAfterOpeningMonths;

    @Column(name = "starting_weight_grams", precision = 5, scale = 2)
    private BigDecimal startingWeightGrams;

    @Column(name = "current_weight_grams", precision = 5, scale = 2)
    private BigDecimal currentWeightGrams;

    private Integer rating;

    @Column(name = "is_finished", nullable = false)
    @Builder.Default
    private boolean isFinished = false;
}
