package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.NotFoundException;

public class ReviewAbsenceException extends NotFoundException {
    public ReviewAbsenceException(String message) {
        super(message);
    }

    public ReviewAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
