package io.everyonecodes.project_module.repositories;

import io.everyonecodes.project_module.models.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
    // usage logs for a product, newest to oldest
    List<UsageLog> findByProductIdOrderByUseDateDesc(Long productId);

    // usage logs of a project,  newest to oldest
    List<UsageLog> findByProjectIdOrderByUseDateDesc(Long projectId);

    int countByProductId(Long productId);
    int countByProductIdAndProjectId(Long productId, Long projectId);
}
