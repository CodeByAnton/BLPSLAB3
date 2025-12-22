package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.NotFoundException;

public class UserAbsenceException extends NotFoundException {
    public UserAbsenceException(String message) {
        super(message);
    }

    public UserAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
