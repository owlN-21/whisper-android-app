package org.example.audiosummary.summary.service;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.summary.dto.SummaryResultResponse;

public interface SummaryService {
    void saveSummary(ProcessingTask task, String content);
    void createStubSummary(ProcessingTask task);
    void deleteByTaskId(Long taskId);
    SummaryResultResponse getResultByTaskId(Long taskId);
}