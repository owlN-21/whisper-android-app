package org.example.audiosummary.processing.service;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.TaskStatus;
import org.example.audiosummary.processing.client.ProcessingServiceClient;
import org.example.audiosummary.processing.dto.SummaryResultResponse;
import org.example.audiosummary.processing.dto.TranscriptionResultResponse;
import org.example.audiosummary.processing.dto.status.ProcessingStatus;
import org.example.audiosummary.summary.service.SummaryService;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
public class AudioProcessingService {
    private final ProcessingServiceClient processingServiceClient;
    private final SummaryService summaryService;

    public AudioProcessingService(ProcessingServiceClient processingServiceClient, SummaryService summaryService) {
        this.processingServiceClient = processingServiceClient;
        this.summaryService = summaryService;
    }

    public void startProcessing(ProcessingTask task) {
        try {
            processingServiceClient.createTranscription(
                    task.getId(),
                    Path.of(task.getStoragePath())
            );

            task.setStatus(TaskStatus.TRANSCRIBING);
            task.setUpdatedAt(LocalDateTime.now());

        } catch (RuntimeException e) {
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage("Failed to start transcription: " + e.getMessage());
            task.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void checkProcessingResult(ProcessingTask task) {
        switch (task.getStatus()) {
            case TRANSCRIBING:
                checkTranscription(task);
                break;

            case SUMMARIZING:
                checkSummary(task);
                break;

            default:
                break;
        }
    }

    private void checkTranscription(ProcessingTask task) {
        TranscriptionResultResponse response =
                processingServiceClient.getTranscription(task.getId());

        if (response.status() == ProcessingStatus.IN_PROGRESS) {
            return;
        }

        if (response.status() == ProcessingStatus.FAILED) {
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(response.errorMessage());
            task.setUpdatedAt(LocalDateTime.now());
            return;
        }

        if (response.status() == ProcessingStatus.COMPLETED) {
            String transcriptText = response.text();

            // тут позже сохраним transcript в БД

            processingServiceClient.createSummary(
                    task.getId(),
                    transcriptText
            );

            task.setStatus(TaskStatus.SUMMARIZING);
            task.setUpdatedAt(LocalDateTime.now());
        }
    }

    private void checkSummary(ProcessingTask task) {
        SummaryResultResponse response =
                processingServiceClient.getSummary(task.getId());

        if (response.status() == ProcessingStatus.IN_PROGRESS) {
            return;
        }

        if (response.status() == ProcessingStatus.FAILED) {
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(response.errorMessage());
            task.setUpdatedAt(LocalDateTime.now());
            return;
        }

        if (response.status() == ProcessingStatus.COMPLETED) {
            summaryService.saveSummary(
                    task,
                    response.content()
            );

            task.setStatus(TaskStatus.COMPLETED);
            task.setUpdatedAt(LocalDateTime.now());
        }
    }
}
