package com.blpsteam.blpslab1.service;

import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.Role;

public interface UserService {
    User register(String username, String password, Role role);
    String login(String username, String password);
    Long getUserIdFromContext();
    Role getUserRoleFromContext();
}

