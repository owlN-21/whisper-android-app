package org.example.audiosummary.processing.scheduler;

import java.util.List;
import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.TaskStatus;
import org.example.audiosummary.processing.service.AudioProcessingService;
import org.example.audiosummary.task.repository.ProcessingTaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AudioProcessingScheduler {

    private final ProcessingTaskRepository processingTaskRepository;
    private final AudioProcessingService audioProcessingService;

    public AudioProcessingScheduler(
            ProcessingTaskRepository processingTaskRepository,
            AudioProcessingService audioProcessingService) {
        this.processingTaskRepository = processingTaskRepository;
        this.audioProcessingService = audioProcessingService;
    }

    @Scheduled(fixedDelayString = "${app.processing.scheduler-delay-ms:5000}")
    @Transactional
    public void startUploadedTasks() {
        List<ProcessingTask> tasks =
                processingTaskRepository.findByStatusIn(
                        List.of(TaskStatus.UPLOADED));

        for (ProcessingTask task : tasks) {
            audioProcessingService.startProcessing(task);
        }
    }

    @Scheduled(fixedDelayString = "${app.processing.scheduler-delay-ms:5000}")
    @Transactional
    public void processActiveTasks() {
        List<ProcessingTask> tasks =
                processingTaskRepository.findByStatusIn(
                        List.of(TaskStatus.TRANSCRIBING, TaskStatus.SUMMARIZING));

        for (ProcessingTask task : tasks) {
            audioProcessingService.checkProcessingResult(task);
        }
    }
}