package io.everyonecodes.project_module.repositories;

import io.everyonecodes.project_module.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Find active (isFinished = false) or empty (isFinished = true)
    List<Product> findByUserIdAndIsFinished(Long userId, boolean isFinished);
}
