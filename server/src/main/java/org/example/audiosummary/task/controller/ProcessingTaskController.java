package org.example.audiosummary.task.controller;

import org.example.audiosummary.summary.dto.TaskResultResponse;
import org.example.audiosummary.summary.service.SummaryService;
import org.example.audiosummary.task.dto.ProcessingTaskResponse;
import org.example.audiosummary.task.service.ProcessingTaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProcessingTaskController {

    private final ProcessingTaskService processingTaskService;
    private final SummaryService summaryService;

    public ProcessingTaskController(ProcessingTaskService processingTaskService, SummaryService summaryService) {
        this.processingTaskService = processingTaskService;
        this.summaryService = summaryService;
    }

    @PostMapping(
            value = "/users/{userId}/tasks",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessingTaskResponse uploadAudio(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
    ) {
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
    public TaskResultResponse getTaskResult(@PathVariable Long taskId) {
        return summaryService.getResultByTaskId(taskId);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId) {
        processingTaskService.deleteTask(taskId);
    }


}