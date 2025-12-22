package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.NotFoundException;

public class CartAbsenceException extends NotFoundException {
    public CartAbsenceException(String message) {
        super(message);
    }

    public CartAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
