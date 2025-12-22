package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.NotFoundException;

public class ProductAbsenceException extends NotFoundException {
    public ProductAbsenceException(String message) {
        super(message);
    }

    public ProductAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
