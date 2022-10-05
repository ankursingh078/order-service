package com.microservices.project.exception;

import lombok.Data;

@Data
public class CustomOrderException extends RuntimeException {

    private String errorCode;
    private int status;

    public CustomOrderException(String message, String errorCode, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
