package org.example.audiosummary.summary.repository;

import org.example.audiosummary.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository <Summary, Long>{

    Optional<Summary> findByProcessingTask_Id(Long taskId);
    void deleteByProcessingTask_Id(Long task_id);

}
