package org.example.audiosummary.processing.dto;

public record TranscriptionResultResponse(
        Long taskId,
        String status,
        String text,
        String errorMessage
) {
}