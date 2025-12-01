package com.blpsteam.blpslab1.dto;

import com.blpsteam.blpslab1.data.enums.Role;


public record UserRequestDTO(String username, String password, Role role) {
}
