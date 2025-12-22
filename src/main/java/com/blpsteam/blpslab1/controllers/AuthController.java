package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.dto.UserRequestDTO;
import com.blpsteam.blpslab1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Аутентификация", description = "API для регистрации и входа пользователей")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Регистрация пользователя", description = "Регистрирует нового пользователя в системе")
    @SecurityRequirements
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким именем уже существует")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRequestDTO userRequestDto) {
        log.info("Получен запрос на регистрацию пользователя: {}", userRequestDto.username());
        User user = userService.register(userRequestDto.username(), userRequestDto.password(), userRequestDto.role());
        log.info("Пользователь успешно зарегистрирован через API: {}", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(String.format("Пользователь %s успешно зарегистрирован", user.getUsername()));
    }

    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя и возвращает JWT токен")
    @SecurityRequirements
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход, возвращен JWT токен"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserRequestDTO userRequestDto) {
        log.info("Получен запрос на вход пользователя: {}", userRequestDto.username());
        String token = userService.login(userRequestDto.username(), userRequestDto.password());
        log.info("Пользователь успешно вошел через API: {}", userRequestDto.username());
        return ResponseEntity.ok(token);
    }
}

