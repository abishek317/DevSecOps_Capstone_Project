package com.cap28.planner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TaskDtos {
    public record TaskRequest(@NotBlank @Size(max = 160) String title, @Size(max = 1000) String description,
                              LocalDate dueDate) {
    }

    public record TaskResponse(Long id, String title, String description, LocalDate dueDate, boolean completed,
                               String completedAt) {
    }
}
