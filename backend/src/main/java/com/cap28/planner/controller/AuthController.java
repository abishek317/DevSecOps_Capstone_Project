package com.cap28.planner.controller;

import com.cap28.planner.dto.AuthDtos.*;
import com.cap28.planner.entity.AppUser;
import com.cap28.planner.repository.UserRepository;
import com.cap28.planner.service.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final SessionUser sessionUser;

    public AuthController(UserRepository users, PasswordEncoder encoder, SessionUser sessionUser) {
        this.users = users;
        this.encoder = encoder;
        this.sessionUser = sessionUser;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest r, HttpSession session) {
        if (users.findByEmailIgnoreCase(r.email()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        AppUser u = users.save(new AppUser(r.email().trim().toLowerCase(), r.displayName().trim(), encoder.encode(r.password())));
        session.setAttribute(SessionUser.KEY, u);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(u));
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest r, HttpSession session) {
        AppUser u = users.findByEmailIgnoreCase(r.email()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!encoder.matches(r.password(), u.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        session.setAttribute(SessionUser.KEY, u);
        return toResponse(u);
    }

    @GetMapping("/me")
    public UserResponse me(HttpSession session) {
        return toResponse(sessionUser.require(session));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(AppUser u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getDisplayName());
    }
}
