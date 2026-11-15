package com.cap28.planner.repository;

import com.cap28.planner.entity.PlannerTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<PlannerTask, Long> {
    List<PlannerTask> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<PlannerTask> findByIdAndUserId(Long id, Long userId);
}
