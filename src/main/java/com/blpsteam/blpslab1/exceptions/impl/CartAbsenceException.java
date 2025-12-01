package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.EntityAbsenceException;

public class CartAbsenceException extends EntityAbsenceException {
    public CartAbsenceException(String message) {
        super(message);
    }

    public CartAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
