package org.example.audiosummary.processing.dto;

import org.example.audiosummary.processing.dto.status.ProcessingStatus;

public record ProcessingAcceptedResponse(
        Long taskId,
        ProcessingStatus status
) {
}
