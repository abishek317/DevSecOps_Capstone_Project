package com.cap28.planner.controller;

import com.cap28.planner.dto.HabitDtos.*;
import com.cap28.planner.entity.*;
import com.cap28.planner.repository.*;
import com.cap28.planner.service.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/habits")
public class HabitController {
    private final HabitRepository habits;
    private final CompletionRepository completions;
    private final SessionUser sessionUser;

    public HabitController(HabitRepository h, CompletionRepository c, SessionUser su) {
        habits = h;
        completions = c;
        sessionUser = su;
    }

    @GetMapping
    public List<HabitResponse> list(HttpSession s) {
        Long uid = sessionUser.require(s).getId();
        return habits.findByUserIdOrderByCreatedAtDesc(uid).stream().map(h -> summary(h, uid)).toList();
    }

    @PostMapping
    public ResponseEntity<HabitResponse> create(@Valid @RequestBody HabitRequest r, HttpSession s) {
        Long uid = sessionUser.require(s).getId();
        Habit h = habits.save(new Habit(uid, r.name().trim(), r.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(summary(h, uid));
    }

    @PutMapping("/{id}")
    public HabitResponse update(@PathVariable Long id, @Valid @RequestBody HabitRequest r, HttpSession s) {
        Long uid = sessionUser.require(s).getId();
        Habit h = owned(id, uid);
        h.update(r.name().trim(), r.description());
        return summary(habits.save(h), uid);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession s) {
        Long uid = sessionUser.require(s).getId();
        Habit h = owned(id, uid);
        completions.deleteAll(completions.findByHabitIdAndUserIdOrderByCompletionDateDesc(id, uid));
        habits.delete(h);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    public CompletionResponse complete(@PathVariable Long id, @RequestParam(required = false) LocalDate date, HttpSession s) {
        Long uid = sessionUser.require(s).getId();
        owned(id, uid);
        LocalDate d = date == null ? LocalDate.now() : date;
        completions.findByHabitIdAndUserIdAndCompletionDate(id, uid, d).orElseGet(() -> completions.save(new HabitCompletion(id, uid, d)));
        Habit h = owned(id, uid);
        HabitResponse res = summary(h, uid);
        return new CompletionResponse(id, d, res.currentStreak(), res.longestStreak());
    }

    private Habit owned(Long id, Long uid) {
        return habits.findByIdAndUserId(id, uid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Habit not found"));
    }

    private HabitResponse summary(Habit h, Long uid) {
        List<LocalDate> dates = completions.findByHabitIdAndUserIdOrderByCompletionDateDesc(h.getId(), uid).stream().map(HabitCompletion::getCompletionDate).distinct().sorted(Comparator.reverseOrder()).toList();
        LocalDate today = LocalDate.now();
        boolean todayDone = dates.contains(today);
        int current = 0;
        LocalDate cursor = todayDone ? today : today.minusDays(1);
        while (dates.contains(cursor)) {
            current++;
            cursor = cursor.minusDays(1);
        }
        int longest = 0, run = 0;
        LocalDate prev = null;
        for (LocalDate d : dates.stream().sorted().toList()) {
            if (prev != null && d.equals(prev.plusDays(1))) run++;
            else run = 1;
            longest = Math.max(longest, run);
            prev = d;
        }
        return new HabitResponse(h.getId(), h.getName(), h.getDescription(), current, longest, todayDone);
    }
}
