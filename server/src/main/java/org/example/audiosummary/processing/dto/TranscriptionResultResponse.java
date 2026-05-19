package org.example.audiosummary.processing.dto;

import org.example.audiosummary.processing.dto.status.ProcessingStatus;

public record TranscriptionResultResponse(
    Long taskId, ProcessingStatus status, String text, String errorMessage) {}
