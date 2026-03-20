package org.example.audiosummary.repository;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessingTaskRepository extends JpaRepository<ProcessingTask, Long> {
    List<ProcessingTask> findByUserId(Long userId);
    List<ProcessingTask> findByStatus(TaskStatus status);
}