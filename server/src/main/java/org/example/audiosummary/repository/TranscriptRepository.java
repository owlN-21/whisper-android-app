package org.example.audiosummary.repository;

import java.util.Optional;
import org.example.audiosummary.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptRepository extends JpaRepository<Transcript, Long> {
  Optional<Transcript> findByProcessingTaskId(Long taskId);
}
