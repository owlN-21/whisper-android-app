package org.example.audiosummary.processing.client;

import java.nio.file.Path;
import org.example.audiosummary.processing.dto.ProcessingAcceptedResponse;
import org.example.audiosummary.processing.dto.SummaryResultResponse;
import org.example.audiosummary.processing.dto.TranscriptionResultResponse;

public interface ProcessingServiceClient {
  ProcessingAcceptedResponse createTranscription(Long taskId, Path audioFilePath);

  TranscriptionResultResponse getTranscription(Long taskId);

  ProcessingAcceptedResponse createSummary(Long taskId, String text);

  SummaryResultResponse getSummary(Long taskId);
}
