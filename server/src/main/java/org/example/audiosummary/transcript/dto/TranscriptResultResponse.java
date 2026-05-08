package org.example.audiosummary.transcript.dto;

public record TranscriptResultResponse(
        Long taskId,
        String status,
        String transcript
) {}
