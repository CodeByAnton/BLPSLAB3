package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.CartResponseDTO;
import com.blpsteam.blpslab1.mapper.CartMapper;
import com.blpsteam.blpslab1.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/buyers/carts")
@Tag(name = "Корзина", description = "API для работы с корзиной")
public class CartController {

    @Autowired
    private CartService cartService;

    @Operation(summary = "Получить корзину", description = "Возвращает корзину текущего покупателя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Корзина успешно получена"),
            @ApiResponse(responseCode = "404", description = "Корзина не найдена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart() {
        log.info("Получен запрос на получение корзины покупателя");
        CartResponseDTO cart = CartMapper.toDto(cartService.getCart());
        log.info("Корзина покупателя успешно возвращена");
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Очистить корзину", description = "Очищает корзину покупателя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Корзина успешно очищена"),
            @ApiResponse(responseCode = "400", description = "Невозможно очистить корзину (есть неоплаченный заказ)"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('BUYER')")
    @DeleteMapping
    public ResponseEntity<CartResponseDTO> clearCart() {
        log.info("Получен запрос на очистку корзины покупателя");
        CartResponseDTO cart = CartMapper.toDto(cartService.clearCart());
        log.info("Корзина покупателя успешно очищена");
        return ResponseEntity.ok(cart);
    }

    @Operation(summary = "Создать корзину", description = "Создает новую корзину для покупателя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Корзина успешно создана"),
            @ApiResponse(responseCode = "400", description = "Корзина уже существует"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping
    public ResponseEntity<CartResponseDTO> createCart() {
        log.info("Получен запрос на создание корзины покупателя");
        CartResponseDTO cart = CartMapper.toDto(cartService.createCart());
        log.info("Корзина покупателя успешно создана");
        return ResponseEntity.ok(cart);
    }
}
