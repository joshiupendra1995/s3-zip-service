package com.s3.zip.controller;

import com.s3.zip.service.S3Service;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/file")
public class UploadDownloadController {

    private static final String BUCKET_NAME = "upendra-28-bucket";
    private final S3Service s3Service;

    public UploadDownloadController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    //This post method is for uploading multiple files
    @PostMapping(value = "/upload/{crNumber}/{year}/{month}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> uploadFiles(@PathVariable String crNumber, @PathVariable String year, @PathVariable String month, @RequestPart("files") Flux<FilePart> fileParts) {
        return s3Service.uploadFiles(year, month, crNumber, fileParts);
    }

    //This post method is for uploading zipped file of the multiple files provided
    @PostMapping(value = "/zipAndUploadFiles/{crNumber}/{year}/{month}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> uploadFilesAsZip(@PathVariable String crNumber, @PathVariable String year, @PathVariable String month, @RequestPart("files") Flux<FilePart> fileParts) {
        return s3Service.uploadFilesAsZip(year, month, crNumber, fileParts);
    }

    @GetMapping("/download/{crNumber}/{year}/{month}/{fileName}")
    public Mono<String> getSignedDownloadUrl(@PathVariable String crNumber, @PathVariable String year, @PathVariable String month, @PathVariable String fileName) {
        String signedUrl = s3Service.generatePresignedUrl(BUCKET_NAME, crNumber, year, month, fileName);
        return Mono.just(signedUrl);
    }

}
