package com.s3.zip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class S3Service {

    private static final String BUCKET_NAME = "upendra-28-bucket";
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;


    public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
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


    public Mono<String> uploadFiles(String year, String month, String crNumber, Flux<FilePart> fileParts) {
        return fileParts.flatMap(filePart -> {
            String fileName = filePart.filename();
            try {
                Path tempFile = Files.createTempFile("upload-", "-" + UUID.randomUUID());
                return filePart.transferTo(tempFile).then(Mono.defer(() -> {
                    uploadFile(BUCKET_NAME, crNumber, year, month, tempFile, fileName);
                    try {
                        Files.delete(tempFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return Mono.empty();
                }));
            } catch (Exception e) {
                return Mono.error(new RuntimeException("Failed to upload file: " + fileName, e));
            }
        }).then(Mono.just("Files uploaded successfully to folder"));
    }

    public void uploadFile(String bucketName, String crNumber, String year, String month, Path filePath, String fileName) {
        String key = String.join("/", crNumber, year, month, fileName);

        PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(key).build();

        s3Client.putObject(request, RequestBody.fromFile(filePath));
    }

    public Mono<String> uploadFilesAsZip(String year, String month, String crNumber, Flux<FilePart> fileParts) {
        return fileParts.flatMap(part -> {
            ByteArrayOutputStream fileOutputStream = new ByteArrayOutputStream();
            return DataBufferUtils.write(part.content(), fileOutputStream).then(Mono.defer(() -> Mono.just(new AbstractMap.SimpleEntry<>(part.filename(), fileOutputStream.toByteArray()))));
        }).collectList().flatMap(files -> {
            try (ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(zipOutputStream)) {

                for (Map.Entry<String, byte[]> file : files) {
                    addToZip(zos, file.getKey(), new ByteArrayInputStream(file.getValue()));
                }
                zos.finish();

                String zipFileName = String.format("%s/%s/%s/%s-%s-%s.zip", crNumber, year, month, crNumber, year, month);
                uploadZipToS3(BUCKET_NAME, zipFileName, zipOutputStream.toByteArray());
                return Mono.just("Zipped file uploaded successfully to folder");
            } catch (Exception e) {
                return Mono.error(new RuntimeException("Failed to zip and upload files", e));
            }
        });
    }


    public String generatePresignedUrl(String crNumber, String year, String month, String fileName) {
        String objectKey = String.join("/", crNumber, year, month, fileName);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(BUCKET_NAME).key(objectKey).build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder().getObjectRequest(getObjectRequest).signatureDuration(Duration.ofMinutes(5)).build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}

