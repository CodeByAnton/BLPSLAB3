package com.blpsteam.blpslab1.exceptions;

public class UserBalanceException extends RuntimeException {
    public UserBalanceException(String message) {
        super(message);
    }

    public UserBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
