package com.microservices.project.external.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.project.exception.CustomOrderException;
import com.microservices.project.external.response.ErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class CustomOrderErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("::{}", response.request().url());
        log.info("::{}", response.request().headers());

        try {
            ErrorResponse errorResponse = objectMapper
                    .readValue(response.body().asInputStream()
                    ,ErrorResponse.class);

            return new CustomOrderException(errorResponse.getErrorMessage(),
                    errorResponse.getErrorCode(), response.status());

        } catch (IOException e) {
            throw new CustomOrderException("Internal Server Error",
                    "INTERNAL_SERVER_ERROR",
                    500);
        }

    }
}
