package org.example.audiosummary.transcript.exception;

public class TranscriptNotFoundException extends RuntimeException {
    public TranscriptNotFoundException(String message) {
        super(message);
    }
}
