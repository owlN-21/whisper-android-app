package org.example.audiosummary.healt.service;

import org.example.audiosummary.healt.dto.HealthResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public HealthResponse getHealth() {
        return new HealthResponse("OK", "audio-summary-server");
    }
}
