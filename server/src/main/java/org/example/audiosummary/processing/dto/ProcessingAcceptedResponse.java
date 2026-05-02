package org.example.audiosummary.processing.dto;

public record ProcessingAcceptedResponse(
        Long taskId,
        String status
) {
}
