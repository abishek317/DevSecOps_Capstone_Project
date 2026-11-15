package com.cap28.planner.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "habits")
public class Habit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Habit() {
    }

    public Habit(Long userId, String name, String description) {
        this.userId = userId;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
