package com.ozzz.skip.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    public String storeFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Cannot store an empty file.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException(
                    "File type not allowed. Please upload a JPEG, PNG, WEBP, or GIF.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException(
                    "File size exceeds 5MB limit.");
        }

        String originalFilename = StringUtils
                .cleanPath(file.getOriginalFilename() != null
                        ? file.getOriginalFilename() : "file");

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path targetLocation = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(),
                    targetLocation,
                    StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file: {} → {}", originalFilename, targetLocation);

            return "/uploads/products/" + uniqueFilename;

        } catch (IOException ex) {
            throw new RuntimeException(
                    "Could not store file. Please try again.", ex);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;

        try {
            String filename = fileUrl.replace("/uploads/products/", "");
            Path filePath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize()
                    .resolve(filename);

            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filePath);

        } catch (IOException ex) {
            log.warn("Could not delete file: {}", fileUrl, ex);
        }
    }
}