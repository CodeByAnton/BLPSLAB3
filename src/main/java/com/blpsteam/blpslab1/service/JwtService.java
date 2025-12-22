package com.blpsteam.blpslab1.service;

import com.blpsteam.blpslab1.data.entities.core.User;

public interface JwtService {
    String generateToken(User user);
}

