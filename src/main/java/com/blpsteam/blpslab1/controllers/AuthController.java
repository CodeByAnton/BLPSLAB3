package com.blpsteam.blpslab1.controllers;


import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.dto.UserRequestDTO;
import com.blpsteam.blpslab1.service.UserService;
import com.blpsteam.blpslab1.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtUtil;

    public AuthController(UserService userService, JwtService jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register( @RequestBody UserRequestDTO userRequestDto) {
        User user = userService.register(userRequestDto.username(), userRequestDto.password(), userRequestDto.role());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(String.format("User %s registered successfully", user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequestDTO userRequestDto) {
        String token=userService.login(userRequestDto.username(), userRequestDto.password());
        return ResponseEntity.ok(token);
    }
}

