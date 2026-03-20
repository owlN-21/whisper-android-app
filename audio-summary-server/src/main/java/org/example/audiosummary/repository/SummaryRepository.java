package org.example.audiosummary.repository;

import org.example.audiosummary.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Optional<Summary> findByProcessingTaskId(Long taskId);
}
