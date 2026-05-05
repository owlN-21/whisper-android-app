package org.example.audiosummary.task.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.example.audiosummary.task.exception.FileStorageException;
import org.example.audiosummary.task.exception.InvalidAudioFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {

  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp3", "wav", "m4a");

  private final Path uploadDir;

  public LocalFileStorageService(@Value("${app.storage.upload-dir}") String uploadDir) {
    this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  @Override
  public String saveFile(MultipartFile file) {
    validateFile(file);

    try {
      Files.createDirectories(uploadDir);

      String originalFilename = file.getOriginalFilename();
      String safeFilename = generateStoredFilename(originalFilename);

      Path targetPath = uploadDir.resolve(safeFilename);
      Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

      return targetPath.toString();
    } catch (IOException e) {
      throw new FileStorageException("Failed to store file", e);
    }
  }

  @Override
  public void deleteFile(String storagePath) {
    if (storagePath == null || storagePath.isBlank()) {
      return;
    }

    try {
      Path path = Paths.get(storagePath);
      Files.deleteIfExists(path);
    } catch (IOException e) {
      throw new FileStorageException("Failed to delete file", e);
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null) {
      throw new InvalidAudioFileException("File is required");
    }

    if (file.isEmpty()) {
      throw new InvalidAudioFileException("File is empty");
    }

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.isBlank()) {
      throw new InvalidAudioFileException("Original filename is missing");
    }

    String extension = getExtension(originalFilename);
    if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
      throw new InvalidAudioFileException(
          "Unsupported audio format. Allowed formats: mp3, wav, m4a");
    }
  }

  private String generateStoredFilename(String originalFilename) {
    String extension = getExtension(originalFilename);
    return UUID.randomUUID() + "." + extension;
  }

  private String getExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
      throw new InvalidAudioFileException("File extension is missing");
    }

    return filename.substring(lastDotIndex + 1);
  }
}
