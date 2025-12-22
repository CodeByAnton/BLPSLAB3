package com.blpsteam.blpslab1.exceptions;

public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
