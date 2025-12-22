package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.NotFoundException;

public class CartItemAbsenceException extends NotFoundException {
    public CartItemAbsenceException(String message) {
        super(message);
    }

    public CartItemAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
