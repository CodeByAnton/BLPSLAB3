package com.blpsteam.blpslab1.exceptions.impl;

import com.blpsteam.blpslab1.exceptions.NotFoundException;

public class UsernameNotFoundException extends NotFoundException {
    public UsernameNotFoundException(String message) {
        super(message);
    }

    public UsernameNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

