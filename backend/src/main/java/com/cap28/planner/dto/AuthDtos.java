package com.cap28.planner.dto;

import jakarta.validation.constraints.*;

public class AuthDtos {
    public record RegisterRequest(@Email @NotBlank String email, @NotBlank @Size(min = 2, max = 80) String displayName,
                                  @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record UserResponse(Long id, String email, String displayName) {
    }
}
