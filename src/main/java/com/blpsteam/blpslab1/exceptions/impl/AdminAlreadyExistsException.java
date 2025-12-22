package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.ConflictException;

public class AdminAlreadyExistsException extends ConflictException {
    public AdminAlreadyExistsException(String message) {
        super(message);
    }

    public AdminAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

