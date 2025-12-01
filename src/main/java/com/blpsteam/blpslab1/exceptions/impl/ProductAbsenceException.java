package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.EntityAbsenceException;

public class ProductAbsenceException extends EntityAbsenceException {
    public ProductAbsenceException(String message) {
        super(message);
    }

    public ProductAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
