package org.example.audiosummary.task.repository;

import java.util.List;
import java.util.Optional;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProcessingTaskRepository extends JpaRepository<ProcessingTask, Long> {
  List<ProcessingTask> findByUser_Id(Long userId);

  List<ProcessingTask> findByStatus(TaskStatus status);

  @Query("select t.storagePath from ProcessingTask t where t.id = :taskId")
  Optional<String> findStoragePathById(Long taskId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = "DELETE FROM processing_tasks WHERE id = :taskId", nativeQuery = true)
  void deleteTaskRowById(Long taskId);
}
