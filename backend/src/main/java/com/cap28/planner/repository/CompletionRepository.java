package com.cap28.planner.repository;

import com.cap28.planner.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompletionRepository extends JpaRepository<HabitCompletion, Long> {
    List<HabitCompletion> findByHabitIdAndUserIdOrderByCompletionDateDesc(Long habitId, Long userId);

    Optional<HabitCompletion> findByHabitIdAndUserIdAndCompletionDate(Long habitId, Long userId, LocalDate date);
}
