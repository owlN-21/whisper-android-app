package org.example.audiosummary.summary.service;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.Summary;
import org.example.audiosummary.summary.dto.SummaryResultResponse;
import org.example.audiosummary.summary.exception.SummaryNotFoundException;
import org.example.audiosummary.summary.repository.SummaryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SummaryServiceImpl implements SummaryService {

    private final SummaryRepository summaryRepository;

    public SummaryServiceImpl(SummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    @Override
    public void saveSummary(ProcessingTask task, String content) {
        Summary summary = new Summary();
        summary.setProcessingTask(task);
        task.setSummary(summary);
        summary.setContent(content);
        summary.setCreatedAt(LocalDateTime.now());

        summaryRepository.save(summary);
    }

    @Override
    public void deleteByTaskId(Long taskId) {
        summaryRepository.deleteByProcessingTask_Id(taskId);
    }

    @Override
    public SummaryResultResponse getResultByTaskId(Long taskId) {
        Summary summary = summaryRepository.findByProcessingTask_Id(taskId)
                .orElseThrow(() -> new SummaryNotFoundException(taskId));

        return new SummaryResultResponse(
                summary.getProcessingTask().getId(),
                summary.getProcessingTask().getStatus().name(),
                summary.getContent()
        );
    }
}