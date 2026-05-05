package org.example.audiosummary.task.service;

import java.util.List;
import org.example.audiosummary.task.dto.ProcessingTaskResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProcessingTaskService {
  ProcessingTaskResponse uploadAudio(Long userId, MultipartFile file);

  ProcessingTaskResponse getTaskById(Long taskId);

  List<ProcessingTaskResponse> getTasksByUserId(Long userId);

  void deleteTask(Long taskId);
}
