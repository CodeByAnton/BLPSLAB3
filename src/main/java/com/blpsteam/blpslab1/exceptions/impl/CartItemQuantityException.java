package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.ValidationException;

public class CartItemQuantityException extends ValidationException {
    public CartItemQuantityException(String message) {
        super(message);
    }

    public CartItemQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}

