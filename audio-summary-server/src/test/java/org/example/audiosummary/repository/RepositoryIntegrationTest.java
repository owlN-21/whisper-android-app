package org.example.audiosummary.repository;

import org.example.audiosummary.entity.*;
import org.example.audiosummary.repository.ProcessingTaskRepository;
import org.example.audiosummary.repository.SummaryRepository;
import org.example.audiosummary.repository.TranscriptRepository;
import org.example.audiosummary.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@ActiveProfiles("test")
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//class RepositoryIntegrationTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ProcessingTaskRepository processingTaskRepository;
//
//    @Autowired
//    private TranscriptRepository transcriptRepository;
//
//    @Autowired
//    private SummaryRepository summaryRepository;
//
//    @Test
//    @DisplayName("Должен сохранять и читать User, ProcessingTask, Transcript и Summary")
//    void shouldSaveAndReadAllEntities() {
//        LocalDateTime now = LocalDateTime.now();
//
//        User user = new User();
//        user.setEmail("test@example.com");
//        user.setCreatedAt(now);
//        user.setUpdatedAt(now);
//
//        User savedUser = userRepository.save(user);
//
//        ProcessingTask task = new ProcessingTask();
//        task.setUser(savedUser);
//        task.setOriginalFilename("lecture.mp3");
//        task.setStoragePath("/audio/lecture.mp3");
//        task.setStatus(TaskStatus.CREATED);
//        task.setErrorMessage(null);
//        task.setCreatedAt(now);
//        task.setUpdatedAt(now);
//
//        ProcessingTask savedTask = processingTaskRepository.save(task);
//
//        Transcript transcript = new Transcript();
//        transcript.setProcessingTask(savedTask);
//        transcript.setText("Это текст транскрипции");
//        transcript.setCreatedAt(now);
//
//        Transcript savedTranscript = transcriptRepository.save(transcript);
//
//        Summary summary = new Summary();
//        summary.setProcessingTask(savedTask);
//        summary.setContent("Это структурированный конспект");
//        summary.setCreatedAt(now);
//
//        Summary savedSummary = summaryRepository.save(summary);
//
//        Optional<User> foundUser = userRepository.findById(savedUser.getId());
//        Optional<ProcessingTask> foundTask = processingTaskRepository.findById(savedTask.getId());
//        Optional<Transcript> foundTranscript = transcriptRepository.findByProcessingTaskId(savedTask.getId());
//        Optional<Summary> foundSummary = summaryRepository.findByProcessingTaskId(savedTask.getId());
//
//        assertThat(foundUser).isPresent();
//        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
//
//        assertThat(foundTask).isPresent();
//        assertThat(foundTask.get().getOriginalFilename()).isEqualTo("lecture.mp3");
//        assertThat(foundTask.get().getStatus()).isEqualTo(TaskStatus.CREATED);
//        assertThat(foundTask.get().getUser().getId()).isEqualTo(savedUser.getId());
//
//        assertThat(foundTranscript).isPresent();
//        assertThat(foundTranscript.get().getText()).isEqualTo("Это текст транскрипции");
//        assertThat(foundTranscript.get().getProcessingTask().getId()).isEqualTo(savedTask.getId());
//
//        assertThat(foundSummary).isPresent();
//        assertThat(foundSummary.get().getContent()).isEqualTo("Это структурированный конспект");
//        assertThat(foundSummary.get().getProcessingTask().getId()).isEqualTo(savedTask.getId());
//
//        assertThat(savedTranscript.getId()).isNotNull();
//        assertThat(savedSummary.getId()).isNotNull();
//    }
//
//    @Test
//    void shouldFindTasksByStatus() {
//        LocalDateTime now = LocalDateTime.now();
//
//        User user = new User();
//        user.setEmail("status-test@example.com");
//        user.setCreatedAt(now);
//        user.setUpdatedAt(now);
//        user = userRepository.save(user);
//
//        ProcessingTask task = new ProcessingTask();
//        task.setUser(user);
//        task.setOriginalFilename("audio.wav");
//        task.setStoragePath("/audio/audio.wav");
//        task.setStatus(TaskStatus.COMPLETED);
//        task.setCreatedAt(now);
//        task.setUpdatedAt(now);
//
//        processingTaskRepository.save(task);
//
//        var tasks = processingTaskRepository.findByStatus(TaskStatus.COMPLETED);
//
//        assertThat(tasks).isNotEmpty();
//        assertThat(tasks.get(0).getStatus()).isEqualTo(TaskStatus.COMPLETED);
//    }
//}