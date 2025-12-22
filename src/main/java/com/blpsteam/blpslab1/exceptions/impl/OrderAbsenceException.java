package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.NotFoundException;

public class OrderAbsenceException extends NotFoundException {
    public OrderAbsenceException(String message) {
        super(message);
    }

    public OrderAbsenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
