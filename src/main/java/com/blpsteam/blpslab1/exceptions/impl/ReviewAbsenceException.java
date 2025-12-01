package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.EntityAbsenceException;

public class ReviewAbsenceException extends EntityAbsenceException {
    public ReviewAbsenceException(String message) {
        super(message);
    }

    public ReviewAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
