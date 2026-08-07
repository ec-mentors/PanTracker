package io.everyonecodes.project_module.repositories;

import io.everyonecodes.project_module.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>  {
    Optional<Category> findByName(String name);
}
