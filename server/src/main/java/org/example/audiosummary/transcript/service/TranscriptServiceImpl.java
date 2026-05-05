package org.example.audiosummary.transcript.service;

import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.Transcript;
import org.example.audiosummary.transcript.dto.TranscriptResultResponse;
import org.example.audiosummary.transcript.exception.TranscriptNotFoundException;
import org.example.audiosummary.transcript.repository.TranscriptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TranscriptServiceImpl implements TranscriptService{
    private final TranscriptRepository transcriptRepository;

    public TranscriptServiceImpl(TranscriptRepository transcriptRepository) {
        this.transcriptRepository = transcriptRepository;
    }

    @Override
    public void saveTranscript(ProcessingTask task, String content) {
        Transcript transcript = new Transcript();
        transcript.setProcessingTask(task);
        task.setTranscript(transcript);
        transcript.setText(content);
        transcript.setCreatedAt(LocalDateTime.now());

        transcriptRepository.save(transcript);

    }

    @Override
    public void deleteByTaskId(Long taskId) {
        transcriptRepository.deleteByProcessingTask_Id(taskId);
    }

    @Override
    public TranscriptResultResponse getResultByTaskId(Long taskId) {
        Transcript transcript  = transcriptRepository.findByProcessingTask_Id(taskId)
                .orElseThrow(() -> new TranscriptNotFoundException(taskId));


        return new TranscriptResultResponse(
                transcript.getProcessingTask().getId(),
                transcript.getProcessingTask().getStatus().name(),
                transcript.getText()
        );
    }
}
