package com.s3.zip.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.s3.zip.service.S3Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/zipFilesAndUpload")
    public String zipAndUpload(@RequestBody JsonNode inputNode) {
        try {
            String bucketName = inputNode.get("bucketName").asText();
            String outputZipKey = inputNode.get("outputZipKey").asText();
            String fileKeys = inputNode.get("fileKeys").asText();
            s3Service.zipFilesAndUpload(bucketName, outputZipKey, fileKeys);
            return "Zipped and uploaded successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}

