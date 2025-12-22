package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.dto.UserRequestDTO;
import com.blpsteam.blpslab1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRequestDTO userRequestDto) {
        User user = userService.register(userRequestDto.username(), userRequestDto.password(), userRequestDto.role());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(String.format("Пользователь %s успешно зарегистрирован", user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserRequestDTO userRequestDto) {
        String token = userService.login(userRequestDto.username(), userRequestDto.password());
        return ResponseEntity.ok(token);
    }
}

