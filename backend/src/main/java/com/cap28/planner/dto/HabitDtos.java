package com.cap28.planner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class HabitDtos {
    public record HabitRequest(@NotBlank @Size(max = 160) String name, @Size(max = 500) String description) {
    }

    public record HabitResponse(Long id, String name, String description, int currentStreak, int longestStreak,
                                boolean completedToday) {
    }

    public record CompletionResponse(Long habitId, LocalDate completionDate, int currentStreak, int longestStreak) {
    }
}
