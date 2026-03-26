package org.example.audiosummary.task.dto;

import java.time.LocalDateTime;

public class ProcessingTaskResponse {
    private Long id;
    private Long userId;
    private String originalFilename;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
