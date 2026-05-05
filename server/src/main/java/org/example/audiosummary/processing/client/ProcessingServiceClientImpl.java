package org.example.audiosummary.processing.client;

import org.example.audiosummary.processing.config.ProcessingServiceProperties;
import org.example.audiosummary.processing.dto.ProcessingAcceptedResponse;
import org.example.audiosummary.processing.dto.SummaryCreateRequest;
import org.example.audiosummary.processing.dto.SummaryResultResponse;
import org.example.audiosummary.processing.dto.TranscriptionResultResponse;
import org.example.audiosummary.processing.exception.ProcessingServiceException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;


//1. POST /api/v1/transcriptions — отправить аудио
//2. GET  /api/v1/transcriptions/{taskId} — получить транскрибацию
//3. POST /api/v1/summaries — отправить текст на summary
//4. GET  /api/v1/summaries/{taskId} — получить summary

@Component
public class ProcessingServiceClientImpl implements ProcessingServiceClient {
    private final RestClient restClient;

    public ProcessingServiceClientImpl(
            RestClient.Builder restClientBuilder,
            ProcessingServiceProperties properties
    ) {
        this.restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Override
    public ProcessingAcceptedResponse createTranscription(Long taskId, Path audioFilePath) {

        if (!Files.exists(audioFilePath)) {
            throw new ProcessingServiceException("Audio file not found: " + audioFilePath);
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("taskId", taskId);
        body.add("file", new FileSystemResource(audioFilePath));

        try{
            return restClient
                    .post()
                    .uri("/api/v1/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body) // request body
                    .retrieve()
                    .body(ProcessingAcceptedResponse.class); // response body
        } catch (RuntimeException e){
            throw new ProcessingServiceException(
                    "Failed to post transcription for task id = " + taskId,
                    e
            );
        }


    }

    @Override
    public TranscriptionResultResponse getTranscription(Long taskId) {
        try {
            return restClient
                    .get()
                    .uri("/api/v1/transcriptions/{taskId}", taskId)
                    .retrieve()
                    .body(TranscriptionResultResponse.class);
        } catch (RuntimeException e) {
            throw new ProcessingServiceException(
                    "Failed to get transcription for task id = " + taskId,
                    e
            );
        }
    }

    @Override
    public ProcessingAcceptedResponse createSummary(Long taskId, String text) {
        if (text == null || text.isBlank()) {
            throw new ProcessingServiceException(
                    "Cannot create summary for empty transcription text, task id = " + taskId
            );
        }

        SummaryCreateRequest request = new SummaryCreateRequest(taskId, text);
        try{
            return restClient
                    .post()
                    .uri("/api/v1/summaries")
                    .body(request)
                    .retrieve()
                    .body(ProcessingAcceptedResponse.class);
        } catch (RuntimeException e){
            throw new ProcessingServiceException(
                    "Failed to post summary for task id = " + taskId,
                    e
            );
        }


    }

    @Override
    public SummaryResultResponse getSummary(Long taskId) {
        try {
            return restClient
                    .get()
                    .uri("/api/v1/summaries/{taskId}", taskId)
                    .retrieve()
                    .body(SummaryResultResponse.class);
        } catch (RuntimeException e){
            throw new ProcessingServiceException(
                    "Failed to get summary for task id = " + taskId,
                    e
            );
        }

    }
}
