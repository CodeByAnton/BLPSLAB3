package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.BusinessException;

public class OrderPaymentException extends BusinessException {
    public OrderPaymentException(String message) {
        super(message);
    }

    public OrderPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}

