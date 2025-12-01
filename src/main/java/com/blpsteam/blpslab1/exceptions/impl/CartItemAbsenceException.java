package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.EntityAbsenceException;

public class CartItemAbsenceException extends EntityAbsenceException {
    public CartItemAbsenceException(String message) {
        super(message);
    }

    public CartItemAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
