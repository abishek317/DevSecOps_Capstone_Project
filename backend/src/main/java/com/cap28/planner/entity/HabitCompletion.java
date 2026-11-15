package com.cap28.planner.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "habit_completions", uniqueConstraints = @UniqueConstraint(columnNames = {"habit_id", "completion_date"}))
public class HabitCompletion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "habit_id", nullable = false)
    private Long habitId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "completion_date", nullable = false)
    private LocalDate completionDate;

    public HabitCompletion() {
    }

    public HabitCompletion(Long habitId, Long userId, LocalDate date) {
        this.habitId = habitId;
        this.userId = userId;
        this.completionDate = date;
    }

    public Long getId() {
        return id;
    }

    public Long getHabitId() {
        return habitId;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }
}
