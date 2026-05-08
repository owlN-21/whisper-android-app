package org.example.audiosummary.processing.dto;

public record SummaryResultResponse(
        Long taskId,
        String status,
        String content,
        String errorMessage
) {
}