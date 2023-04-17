package com.example.task1jwt.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = {"*"}, maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping({"/all"})
    public String publicContent() {
        return "Public content.";
    }

    @GetMapping({"/user"})
    @PreAuthorize("hasRole('ROLE_USER')")
    public String userContent() {
        return "User content.";
    }

    @GetMapping({"/moderator"})
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public String moderatorContent() {
        return "Moderator content.";
    }

    @GetMapping({"/admin"})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String adminContent() {
        return "Admin content.";
    }
}
