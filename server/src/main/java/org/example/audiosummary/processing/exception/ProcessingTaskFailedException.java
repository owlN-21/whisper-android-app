package org.example.audiosummary.processing.exception;

public class ProcessingTaskFailedException extends RuntimeException {

  public ProcessingTaskFailedException(Long taskId, String errorMessage) {
    super("Processing task " + taskId + " failed: " + errorMessage);
  }
}
