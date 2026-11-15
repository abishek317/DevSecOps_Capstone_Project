package com.cap28.planner.service;

import com.cap28.planner.entity.AppUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionUser {
    public static final String KEY = "plannerUser";

    public AppUser require(HttpSession session) {
        Object value = session.getAttribute(KEY);
        if (!(value instanceof AppUser user))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Please log in");
        return user;
    }
}
