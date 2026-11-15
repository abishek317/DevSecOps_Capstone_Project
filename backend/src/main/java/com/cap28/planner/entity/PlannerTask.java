package com.cap28.planner.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "planner_tasks")
public class PlannerTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(length = 1000)
    private String description;
    @Column
    private LocalDate dueDate;
    @Column(nullable = false)
    private boolean completed = false;
    @Column
    private LocalDateTime completedAt;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PlannerTask() {
    }

    public PlannerTask(Long userId, String title, String description, LocalDate dueDate) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void update(String title, String description, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.completedAt = completed ? LocalDateTime.now() : null;
    }
}
