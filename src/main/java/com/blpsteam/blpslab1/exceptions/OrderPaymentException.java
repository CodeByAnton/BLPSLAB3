package com.blpsteam.blpslab1.exceptions;

public class OrderPaymentException extends RuntimeException {
    public OrderPaymentException(String message) {
        super(message);
    }

    public OrderPaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
