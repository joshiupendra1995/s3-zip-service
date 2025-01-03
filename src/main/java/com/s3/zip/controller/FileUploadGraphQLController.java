package com.s3.zip.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class FileUploadGraphQLController {

    private final WebClient webClient;
    private final String baseUrl;

    public FileUploadGraphQLController(WebClient.Builder webClientBuilder, @Value("${base.url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.baseUrl = baseUrl;
    }

    @MutationMapping
    public Mono<String> uploadFilesAsZip(@Argument String crNumber, @Argument String year, @Argument String month, @Argument Flux<FilePart> files) {
        return webClient.post().uri("/zipAndUploadFiles/{crNumber}/{year}/{month}", crNumber, year, month)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData("files", files))
                .retrieve().bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> Mono.just("Error uploading files: " + ex.getMessage()));
    }

    @QueryMapping
    public Mono<String> getSignedDownloadUrl(@Argument String crNumber, @Argument String year, @Argument String month, @Argument String fileName) {
        //return Mono.just(s3Service.generatePresignedUrl(crNumber, year, month, fileName));
        return Mono.just("");
    }
}

