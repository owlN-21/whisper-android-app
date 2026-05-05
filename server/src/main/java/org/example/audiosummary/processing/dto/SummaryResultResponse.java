package org.example.audiosummary.processing.dto;

import org.example.audiosummary.processing.dto.status.ProcessingStatus;

public record SummaryResultResponse(
    Long taskId, ProcessingStatus status, String content, String errorMessage) {}
