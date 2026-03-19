package org.example.audiosummary.service;

import org.example.audiosummary.dto.HealthResponse;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public HealthResponse getHealth() {
        return new HealthResponse("OK", "audio-summary-server");
    }
}
