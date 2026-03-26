package org.example.audiosummary.task.service;

import org.example.audiosummary.task.dto.ProcessingTaskResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProcessingTaskService {
    ProcessingTaskResponse uploadAudio(Long userId, MultipartFile file);
    ProcessingTaskResponse getTaskById(Long taskId);
    List<ProcessingTaskResponse> getTasksByUserId(Long userId);
}