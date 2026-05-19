package org.example.audiosummary.processing.exception;

public class ProcessingServiceException extends RuntimeException {

  public ProcessingServiceException(String message) {
    super(message);
  }

  public ProcessingServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
