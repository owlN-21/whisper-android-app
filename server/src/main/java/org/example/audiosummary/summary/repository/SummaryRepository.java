package org.example.audiosummary.summary.repository;

import java.util.Optional;
import org.example.audiosummary.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

  Optional<Summary> findByProcessingTask_Id(Long taskId);

  void deleteByProcessingTask_Id(Long task_id);
}
