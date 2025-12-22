package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.OrderResponseDTO;
import com.blpsteam.blpslab1.mapper.OrderMapper;
import com.blpsteam.blpslab1.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/buyers/orders")
@Tag(name = "Заказы", description = "API для работы с заказами")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Создать заказ", description = "Создает новый заказ из корзины и возвращает ссылку на оплату")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Заказ успешно создан"),
            @ApiResponse(responseCode = "400", description = "Корзина пуста или есть неоплаченный заказ"),
            @ApiResponse(responseCode = "404", description = "Корзина не найдена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder() {
        log.info("Получен запрос на создание заказа");
        String paymentLink = orderService.createOrder();
        log.info("Заказ успешно создан. Ссылка на оплату сгенерирована");
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderMapper.toDto(paymentLink));
    }
}
