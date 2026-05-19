package org.example.audiosummary.task.mapper;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.task.dto.ProcessingTaskResponse;
import org.springframework.stereotype.Component;

@Component
public class ProcessingTaskMapper {

  public ProcessingTaskResponse toResponse(ProcessingTask task) {
    return new ProcessingTaskResponse(
        task.getId(),
        task.getUser().getId(),
        task.getOriginalFilename(),
        task.getStatus().name(),
        task.getErrorMessage(),
        task.getCreatedAt(),
        task.getUpdatedAt());
  }
}
