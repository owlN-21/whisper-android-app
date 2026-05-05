package org.example.audiosummary.summary.service;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.Summary;
import org.example.audiosummary.summary.dto.TaskResultResponse;

public interface SummaryService {
    void saveSummary(ProcessingTask task, String content);
    void createStubSummary(ProcessingTask task);
    void deleteByTaskId(Long taskId);
    TaskResultResponse getResultByTaskId(Long taskId);
}