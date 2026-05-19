package org.example.audiosummary.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transcripts")
public class Transcript {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "task_id", nullable = false, unique = true)
  private ProcessingTask processingTask;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String text;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public Transcript() {}

  public Transcript(ProcessingTask processingTask, String text, LocalDateTime createdAt) {
    this.processingTask = processingTask;
    this.text = text;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public ProcessingTask getProcessingTask() {
    return processingTask;
  }

  public String getText() {
    return text;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setProcessingTask(ProcessingTask processingTask) {
    this.processingTask = processingTask;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
