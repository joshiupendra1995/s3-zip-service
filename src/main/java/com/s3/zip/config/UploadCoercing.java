package com.s3.zip.config;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.springframework.web.multipart.MultipartFile;

public class UploadCoercing implements Coercing<MultipartFile, String> {

    @Override
    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
        if (dataFetcherResult instanceof MultipartFile) {
            return ((MultipartFile) dataFetcherResult).getOriginalFilename();
        }
        throw new CoercingSerializeException("Expected a MultipartFile object");
    }

    @Override
    public MultipartFile parseValue(Object input) throws CoercingParseValueException {
        if (input instanceof MultipartFile) {
            return (MultipartFile) input;
        }
        throw new CoercingParseValueException("Expected a MultipartFile object");
    }

    @Override
    public MultipartFile parseLiteral(Object input) throws CoercingParseLiteralException {
        throw new CoercingParseLiteralException("Parsing literals is not supported for file uploads");
    }
}

