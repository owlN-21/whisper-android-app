package org.example.audiosummary.task.repository;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessingTaskRepository extends JpaRepository<ProcessingTask, Long> {
    List<ProcessingTask> findByUser_Id(Long userId);
    List<ProcessingTask> findByStatus(TaskStatus status);
}