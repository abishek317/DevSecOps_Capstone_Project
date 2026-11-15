package com.cap28.planner.controller;

import com.cap28.planner.dto.TaskDtos.*;
import com.cap28.planner.entity.*;
import com.cap28.planner.repository.TaskRepository;
import com.cap28.planner.service.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskRepository tasks;
    private final SessionUser sessionUser;

    public TaskController(TaskRepository tasks, SessionUser sessionUser) {
        this.tasks = tasks;
        this.sessionUser = sessionUser;
    }

    @GetMapping
    public List<TaskResponse> list(HttpSession s) {
        Long uid = sessionUser.require(s).getId();
        return tasks.findByUserIdOrderByCreatedAtDesc(uid).stream().map(this::dto).toList();
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest r, HttpSession s) {
        PlannerTask t = tasks.save(new PlannerTask(sessionUser.require(s).getId(), r.title().trim(), r.description(), r.dueDate()));
        return ResponseEntity.status(HttpStatus.CREATED).body(dto(t));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest r, HttpSession s) {
        PlannerTask t = owned(id, s);
        t.update(r.title().trim(), r.description(), r.dueDate());
        return dto(tasks.save(t));
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean completed, HttpSession s) {
        PlannerTask t = owned(id, s);
        t.setCompleted(completed);
        return dto(tasks.save(t));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession s) {
        tasks.delete(owned(id, s));
        return ResponseEntity.noContent().build();
    }

    private PlannerTask owned(Long id, HttpSession s) {
        return tasks.findByIdAndUserId(id, sessionUser.require(s).getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private TaskResponse dto(PlannerTask t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), t.getDueDate(), t.isCompleted(), t.getCompletedAt() == null ? null : t.getCompletedAt().toString());
    }
}
