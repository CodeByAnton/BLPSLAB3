package com.blpsteam.blpslab1.exceptions;

public class CartItemQuantityException extends RuntimeException {
    public CartItemQuantityException(String message) {
        super(message);
    }

    public CartItemQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}
