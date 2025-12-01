package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.EntityAbsenceException;

public class UserAbsenceException extends EntityAbsenceException {
    public UserAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserAbsenceException(String message) {
        super(message);
    }
}
