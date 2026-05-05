package org.example.audiosummary.transcript.service;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.transcript.dto.TranscriptResultResponse;

public interface TranscriptService {
    void saveTranscript(ProcessingTask task, String content);
    void deleteByTaskId(Long taskId);
    TranscriptResultResponse getResultByTaskId(Long taskId);
}
