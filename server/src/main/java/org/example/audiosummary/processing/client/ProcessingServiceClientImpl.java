package org.example.audiosummary.processing.client;

import java.nio.file.Files;
import java.nio.file.Path;
import org.example.audiosummary.processing.config.ProcessingServiceProperties;
import org.example.audiosummary.processing.dto.ProcessingAcceptedResponse;
import org.example.audiosummary.processing.dto.SummaryCreateRequest;
import org.example.audiosummary.processing.dto.SummaryResultResponse;
import org.example.audiosummary.processing.dto.TranscriptionResultResponse;
import org.example.audiosummary.processing.exception.ProcessingServiceException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProcessingServiceClientImpl implements ProcessingServiceClient {
    private final RestClient restClient;
    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public ProcessingServiceClientImpl(
            RestClient.Builder restClientBuilder,
            ProcessingServiceProperties properties
    ) {
        this.baseUrl = properties.getBaseUrl();
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
        body.add("taskId", taskId.toString());
        body.add("model", "tone");
        body.add("file", new FileSystemResource(audioFilePath.toFile()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<ProcessingAcceptedResponse> response =
                    restTemplate.postForEntity(
                            baseUrl + "/api/v1/transcriptions",
                            requestEntity,
                            ProcessingAcceptedResponse.class
                    );

            return response.getBody();

        } catch (RestClientResponseException e) {
            throw new ProcessingServiceException(
                    "Failed to post transcription for task id = " + taskId
                            + ". Status = " + e.getStatusCode()
                            + ". Response = " + e.getResponseBodyAsString(),
                    e
            );

        } catch (RuntimeException e) {
            throw new ProcessingServiceException(
                    "Failed to post transcription for task id = " + taskId
                            + ". Cause = " + e.getMessage(),
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

        } catch (RestClientResponseException e) {
            throw new ProcessingServiceException(
                    "Failed to get transcription for task id = " + taskId
                            + ". Status = " + e.getStatusCode()
                            + ". Response = " + e.getResponseBodyAsString(),
                    e
            );

        } catch (RuntimeException e) {
            throw new ProcessingServiceException(
                    "Failed to get transcription for task id = " + taskId
                            + ". Cause = " + e.getMessage(),
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SummaryCreateRequest> requestEntity =
                new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ProcessingAcceptedResponse> response =
                    restTemplate.postForEntity(
                            baseUrl + "/api/v1/summaries",
                            requestEntity,
                            ProcessingAcceptedResponse.class
                    );

            return response.getBody();

        } catch (RestClientResponseException e) {
            throw new ProcessingServiceException(
                    "Failed to post summary for task id = " + taskId
                            + ". Status = " + e.getStatusCode()
                            + ". Response = " + e.getResponseBodyAsString(),
                    e
            );

        } catch (RuntimeException e) {
            throw new ProcessingServiceException(
                    "Failed to post summary for task id = " + taskId
                            + ". Cause = " + e.getMessage(),
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

        } catch (RestClientResponseException e) {
            throw new ProcessingServiceException(
                    "Failed to get summary for task id = " + taskId
                            + ". Status = " + e.getStatusCode()
                            + ". Response = " + e.getResponseBodyAsString(),
                    e
            );

        } catch (RuntimeException e) {
            throw new ProcessingServiceException(
                    "Failed to get summary for task id = " + taskId
                            + ". Cause = " + e.getMessage(),
                    e
            );
        }
    }
}
