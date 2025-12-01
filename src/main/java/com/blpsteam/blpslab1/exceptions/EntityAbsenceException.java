package com.blpsteam.blpslab1.exceptions;

public class EntityAbsenceException extends RuntimeException {
    public EntityAbsenceException(String message) {
        super(message);
    }

    public EntityAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
