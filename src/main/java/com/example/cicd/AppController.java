package com.example.cicd;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "message", "CI/CD Demo App is running",
                "status", "healthy");
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "cicd-demo");
    }

    @GetMapping("/api/version")
    public Map<String, String> version() {
        return Map.of(
                "app", "cicd-demo",
                "version", "1.0.0",
                "environment", "development");
    }
}
