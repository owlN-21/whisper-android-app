package org.example.audiosummary.task.repository;

import java.util.List;
import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessingTaskRepository extends JpaRepository<ProcessingTask, Long> {
  List<ProcessingTask> findByUser_Id(Long userId);

  List<ProcessingTask> findByStatus(TaskStatus status);
}
