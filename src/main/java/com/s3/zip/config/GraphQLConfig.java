package com.s3.zip.config;

import graphql.schema.GraphQLScalarType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

@Configuration
public class GraphQLConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return builder -> builder.scalar(UploadScalar());
    }

    private GraphQLScalarType UploadScalar() {
        return GraphQLScalarType.newScalar()
                .name("Upload")
                .description("A scalar to handle file uploads")
                .coercing(new UploadCoercing())
                .build();
    }
}

