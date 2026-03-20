package org.example.audiosummary.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_tasks")
public class ProcessingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TaskStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "processingTask", fetch = FetchType.LAZY)
    private Transcript transcript;

    @OneToOne(mappedBy = "processingTask", fetch = FetchType.LAZY)
    private Summary summary;

    public ProcessingTask() {
    }

    public ProcessingTask(
            User user,
            String originalFilename,
            String storagePath,
            TaskStatus status,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.user = user;
        this.originalFilename = originalFilename;
        this.storagePath = storagePath;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Transcript getTranscript() {
        return transcript;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setTranscript(Transcript transcript) {
        this.transcript = transcript;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }
}
