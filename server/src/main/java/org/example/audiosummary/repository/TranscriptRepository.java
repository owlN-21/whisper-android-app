package org.example.audiosummary.repository;


import org.example.audiosummary.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TranscriptRepository extends JpaRepository<Transcript, Long> {
    Optional<Transcript> findByProcessingTaskId(Long taskId);
}