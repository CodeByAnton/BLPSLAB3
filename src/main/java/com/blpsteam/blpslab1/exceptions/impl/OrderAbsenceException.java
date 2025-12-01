package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.EntityAbsenceException;

public class OrderAbsenceException extends EntityAbsenceException {
    public OrderAbsenceException(String message) {
        super(message);
    }

    public OrderAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
