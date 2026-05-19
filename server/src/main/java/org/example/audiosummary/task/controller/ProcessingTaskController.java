package org.example.audiosummary.task.controller;

import java.util.List;
import org.example.audiosummary.summary.dto.SummaryResultResponse;
import org.example.audiosummary.summary.service.SummaryService;
import org.example.audiosummary.task.dto.ProcessingTaskResponse;
import org.example.audiosummary.task.service.ProcessingTaskService;
import org.example.audiosummary.transcript.dto.TranscriptResultResponse;
import org.example.audiosummary.transcript.service.TranscriptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class ProcessingTaskController {

  private final ProcessingTaskService processingTaskService;
  private final SummaryService summaryService;
  private final TranscriptService transcriptService;

  public ProcessingTaskController(
          ProcessingTaskService processingTaskService, SummaryService summaryService, TranscriptService transcriptService) {
    this.processingTaskService = processingTaskService;
    this.summaryService = summaryService;
      this.transcriptService = transcriptService;
  }

  @PostMapping(value = "/users/{userId}/tasks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ProcessingTaskResponse uploadAudio(
      @PathVariable Long userId, @RequestParam("file") MultipartFile file) {
    return processingTaskService.uploadAudio(userId, file);
  }

  @GetMapping("/tasks/{taskId}")
  public ProcessingTaskResponse getTaskById(@PathVariable Long taskId) {
    return processingTaskService.getTaskById(taskId);
  }

  @GetMapping("/users/{userId}/tasks")
  public List<ProcessingTaskResponse> getTasksByUserId(@PathVariable Long userId) {
    return processingTaskService.getTasksByUserId(userId);
  }

  @GetMapping("/tasks/{taskId}/result")
  public SummaryResultResponse getTaskResult(@PathVariable Long taskId) {
    return summaryService.getResultByTaskId(taskId);
  }

    @GetMapping("/tasks/{taskId}/transcript")
    public TranscriptResultResponse getTaskTranscript(@PathVariable Long taskId) {
        return transcriptService.getResultByTaskId(taskId);
    }

  @DeleteMapping("/tasks/{taskId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTask(@PathVariable Long taskId) {
    processingTaskService.deleteTask(taskId);
  }
}
