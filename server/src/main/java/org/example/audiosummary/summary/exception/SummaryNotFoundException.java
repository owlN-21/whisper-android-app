package org.example.audiosummary.summary.exception;

public class SummaryNotFoundException extends RuntimeException {
    public SummaryNotFoundException(Long taskId) {
        super("Summary not found for task id = " + taskId);
    }
}
