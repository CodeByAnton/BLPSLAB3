package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.ReviewRequestDTO;
import com.blpsteam.blpslab1.service.ReviewService;
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
@RequestMapping("/api/v1/products/{productId}/reviews")
@Tag(name = "Отзывы", description = "API для работы с отзывами на товары")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Operation(summary = "Добавить отзыв", description = "Добавляет отзыв на товар (требуется роль BUYER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отзыв успешно отправлен на обработку"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные (рейтинг должен быть от 0 до 5)"),
            @ApiResponse(responseCode = "404", description = "Товар не найден"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('BUYER')")
    @PostMapping
    public ResponseEntity<String> addReview(@PathVariable Long productId, @RequestBody ReviewRequestDTO dto) {
        log.info("Получен запрос на добавление отзыва. Товар ID: {}, рейтинг: {}", productId, dto.rating());
        reviewService.publishReview(productId, dto.rating());
        log.info("Отзыв на товар с ID {} успешно отправлен на обработку", productId);
        return ResponseEntity.ok("Отзыв отправлен на обработку");
    }
}
