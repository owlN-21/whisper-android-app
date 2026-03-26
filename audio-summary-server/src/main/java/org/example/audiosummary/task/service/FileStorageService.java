package org.example.audiosummary.task.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String saveFile(MultipartFile file);
}