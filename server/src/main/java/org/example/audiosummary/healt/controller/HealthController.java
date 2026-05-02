package org.example.audiosummary.healt.controller;

import org.example.audiosummary.healt.dto.HealthResponse;
import org.example.audiosummary.healt.service.HealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthRervice) {
        this.healthService = healthRervice;
    }

    @GetMapping("/health")
    public HealthResponse health(){
        return healthService.getHealth();
    }
}
