package org.example.audiosummary.processing.service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.TaskStatus;
import org.example.audiosummary.processing.client.ProcessingServiceClient;
import org.example.audiosummary.processing.dto.SummaryResultResponse;
import org.example.audiosummary.processing.dto.TranscriptionResultResponse;
import org.example.audiosummary.processing.dto.status.ProcessingStatus;
import org.example.audiosummary.summary.service.SummaryService;
import org.example.audiosummary.transcript.service.TranscriptService;
import org.springframework.stereotype.Service;

@Service
public class AudioProcessingService {
  private final ProcessingServiceClient processingServiceClient;
  private final SummaryService summaryService;
  private final TranscriptService transcriptService;

  public AudioProcessingService(
      ProcessingServiceClient processingServiceClient,
      SummaryService summaryService,
      TranscriptService transcriptService) {
    this.processingServiceClient = processingServiceClient;
    this.summaryService = summaryService;
    this.transcriptService = transcriptService;
  }

  public void startProcessing(ProcessingTask task) {
    try {
      processingServiceClient.createTranscription(task.getId(), Path.of(task.getStoragePath()));

      task.setStatus(TaskStatus.TRANSCRIBING);
      task.setUpdatedAt(LocalDateTime.now());

    } catch (RuntimeException e) {
      task.setStatus(TaskStatus.FAILED);
      task.setErrorMessage("Failed to start transcription: " + e.getMessage());
      task.setUpdatedAt(LocalDateTime.now());
    }
  }

  public void checkProcessingResult(ProcessingTask task) {
    try {
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
    } catch (RuntimeException e) {
      task.setStatus(TaskStatus.FAILED);
      task.setErrorMessage("Failed to check processing result: " + e.getMessage());
      task.setUpdatedAt(LocalDateTime.now());
    }
  }

  private void checkTranscription(ProcessingTask task) {
    TranscriptionResultResponse response = processingServiceClient.getTranscription(task.getId());

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
      transcriptService.saveTranscript(task, response.text());

      processingServiceClient.createSummary(task.getId(), transcriptText);

      task.setStatus(TaskStatus.SUMMARIZING);
      task.setUpdatedAt(LocalDateTime.now());
    }
  }

  private void checkSummary(ProcessingTask task) {
    SummaryResultResponse response = processingServiceClient.getSummary(task.getId());

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
      summaryService.saveSummary(task, response.content());

      task.setStatus(TaskStatus.COMPLETED);
      task.setUpdatedAt(LocalDateTime.now());
    }
  }
}
