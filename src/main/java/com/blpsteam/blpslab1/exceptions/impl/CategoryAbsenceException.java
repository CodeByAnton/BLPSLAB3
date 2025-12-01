package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.EntityAbsenceException;

public class CategoryAbsenceException extends EntityAbsenceException {
    public CategoryAbsenceException(String message) {
        super(message);
    }

    public CategoryAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
