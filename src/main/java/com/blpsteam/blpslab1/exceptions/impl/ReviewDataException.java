package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.ValidationException;

public class ReviewDataException extends ValidationException {
    public ReviewDataException(String message) {
        super(message);
    }

    public ReviewDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

