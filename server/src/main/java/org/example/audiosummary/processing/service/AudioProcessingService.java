package org.example.audiosummary.processing.service;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.TaskStatus;
import org.example.audiosummary.processing.client.ProcessingServiceClient;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Service
public class AudioProcessingService {
    private final ProcessingServiceClient processingServiceClient;

    public AudioProcessingService(ProcessingServiceClient processingServiceClient) {
        this.processingServiceClient = processingServiceClient;
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
}
