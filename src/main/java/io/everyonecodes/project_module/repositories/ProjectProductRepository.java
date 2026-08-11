package io.everyonecodes.project_module.repositories;

import io.everyonecodes.project_module.models.ProjectProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectProductRepository extends JpaRepository<ProjectProduct, Long> {
    // Find all products of a project
    List<ProjectProduct> findByProjectId(Long projectId);

    long countByProjectId(Long projectId);
}
