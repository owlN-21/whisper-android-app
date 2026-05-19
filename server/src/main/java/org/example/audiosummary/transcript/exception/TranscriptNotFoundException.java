package org.example.audiosummary.transcript.exception;

public class TranscriptNotFoundException extends RuntimeException {
  public TranscriptNotFoundException(Long taskId) {
    super("Transcription not found for task id = " + taskId);
  }
}
