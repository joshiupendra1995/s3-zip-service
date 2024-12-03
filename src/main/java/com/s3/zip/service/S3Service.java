package com.s3.zip.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);
    private final S3Client s3Client;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void zipFilesAndUpload(String bucketName, String outputZipKey, String keys) throws Exception {
        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOutputStream)) {
            for (String key : keys.split(",")) {
                log.info("key :: {}", key);
                InputStream inputStream = fetchFileFromS3(bucketName, key);
                addToZip(zos, key, inputStream);
            }
        }

        uploadZipToS3(bucketName, outputZipKey, zipOutputStream.toByteArray());
    }

    private InputStream fetchFileFromS3(String bucketName, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
        return s3Client.getObject(getObjectRequest);
    }

    private void addToZip(ZipOutputStream zos, String fileName, InputStream inputStream) throws Exception {
        zos.putNextEntry(new ZipEntry(fileName));
        inputStream.transferTo(zos);
        zos.closeEntry();
    }

    private void uploadZipToS3(String bucketName, String key, byte[] zipContent) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(key).build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(zipContent));
    }
}

