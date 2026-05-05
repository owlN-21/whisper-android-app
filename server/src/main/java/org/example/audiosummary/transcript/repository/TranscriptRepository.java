package org.example.audiosummary.transcript.repository;

import org.example.audiosummary.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TranscriptRepository  extends JpaRepository<Transcript, Long> {
    Optional<Transcript> findByProcessingTask_Id(Long taskId);
    void deleteByProcessingTask_Id(Long task_id);
}
