package org.example.audiosummary.task.service;

import java.time.LocalDateTime;
import java.util.List;
import org.example.audiosummary.entity.ProcessingTask;
import org.example.audiosummary.entity.TaskStatus;
import org.example.audiosummary.entity.User;
import org.example.audiosummary.processing.service.AudioProcessingService;
import org.example.audiosummary.summary.service.SummaryService;
import org.example.audiosummary.task.dto.ProcessingTaskResponse;
import org.example.audiosummary.task.exception.ProcessingTaskNotFoundException;
import org.example.audiosummary.task.mapper.ProcessingTaskMapper;
import org.example.audiosummary.task.repository.ProcessingTaskRepository;
import org.example.audiosummary.transcript.service.TranscriptService;
import org.example.audiosummary.user.exception.UserNotFoundException;
import org.example.audiosummary.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProcessingTaskServiceImpl implements ProcessingTaskService {

  private final ProcessingTaskRepository processingTaskRepository;
  private final UserRepository userRepository;
  private final FileStorageService fileStorageService;
  private final ProcessingTaskMapper processingTaskMapper;
  private final SummaryService summaryService;
  private final TranscriptService transcriptService;
  private final AudioProcessingService audioProcessingService;

  public ProcessingTaskServiceImpl(
      ProcessingTaskRepository processingTaskRepository,
      UserRepository userRepository,
      FileStorageService fileStorageService,
      ProcessingTaskMapper processingTaskMapper,
      SummaryService summaryService,
      TranscriptService transcriptService,
      AudioProcessingService audioProcessingService) {
    this.processingTaskRepository = processingTaskRepository;
    this.userRepository = userRepository;
    this.fileStorageService = fileStorageService;
    this.processingTaskMapper = processingTaskMapper;
    this.summaryService = summaryService;
    this.transcriptService = transcriptService;
    this.audioProcessingService = audioProcessingService;
  }

  @Override
  @Transactional
  public ProcessingTaskResponse uploadAudio(Long userId, MultipartFile file) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

    String originalFilename = file.getOriginalFilename();
    String storagePath = fileStorageService.saveFile(file);

    LocalDateTime now = LocalDateTime.now();

    ProcessingTask task =
        new ProcessingTask(
            user, originalFilename, storagePath, TaskStatus.UPLOADED, null, now, now);

    ProcessingTask savedTask = processingTaskRepository.save(task);
    audioProcessingService.startProcessing(savedTask);
    return processingTaskMapper.toResponse(savedTask);
  }

  @Override
  @Transactional
  public ProcessingTaskResponse getTaskById(Long taskId) {
    ProcessingTask task =
        processingTaskRepository
            .findById(taskId)
            .orElseThrow(() -> new ProcessingTaskNotFoundException(taskId));

    audioProcessingService.checkProcessingResult(task);

    return processingTaskMapper.toResponse(task);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProcessingTaskResponse> getTasksByUserId(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }

    return processingTaskRepository.findByUser_Id(userId).stream()
        .map(processingTaskMapper::toResponse)
        .toList();
  }

  @Override
  @Transactional
  public void deleteTask(Long taskId) {
    ProcessingTask task =
        processingTaskRepository
            .findById(taskId)
            .orElseThrow(() -> new ProcessingTaskNotFoundException(taskId));

    transcriptService.deleteByTaskId(taskId);
    summaryService.deleteByTaskId(taskId);
    fileStorageService.deleteFile(task.getStoragePath());
    processingTaskRepository.delete(task);
  }
}
