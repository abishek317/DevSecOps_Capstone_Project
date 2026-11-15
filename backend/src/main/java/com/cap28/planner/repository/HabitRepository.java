package com.cap28.planner.repository;

import com.cap28.planner.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Habit> findByIdAndUserId(Long id, Long userId);
}
