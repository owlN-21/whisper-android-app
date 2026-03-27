package org.example.audiosummary.task.exception;

public class ProcessingTaskNotFoundException extends RuntimeException {

    public ProcessingTaskNotFoundException(Long taskId) {
        super("Processing task not found with id: " + taskId);
    }
}