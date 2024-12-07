package com.s3.zip.controller;

import com.s3.zip.service.S3Service;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/v1/file")
public class UploadDownloadController {

    private final S3Service s3Service;

    private static final String BUCKET_NAME = "upendra-28-bucket";

    public UploadDownloadController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping(value = "/upload/{folderName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadFile(@PathVariable String folderName,
                             @RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        Path tempFile;
        try {
            tempFile = Files.createTempFile("upload-", "-" + UUID.randomUUID());
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            s3Service.uploadFile(BUCKET_NAME, folderName, fileName, tempFile);
            Files.delete(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }

        return String.join(":","File uploaded successfully to folder " ,folderName);
    }

    @GetMapping("/download/{folderName}/{fileName}")
    public ResponseEntity<String> getSignedDownloadUrl(@PathVariable String folderName,
                                                       @PathVariable String fileName) {
        String signedUrl = s3Service.generatePresignedUrl(BUCKET_NAME, folderName, fileName);
        return ResponseEntity.ok(signedUrl);
    }

}
