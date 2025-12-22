package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.ProductResponseDTO;
import com.blpsteam.blpslab1.mapper.ProductMapper;
import com.blpsteam.blpslab1.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/products")
@Tag(name = "Администрирование", description = "API для администраторов")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Получить все товары", description = "Возвращает список всех товаров (включая неодобренные) для администратора")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список товаров успешно получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        log.info("Получен запрос администратора на получение всех товаров");
        Page<ProductResponseDTO> result = ProductMapper.toDtoPage(productService.getAllProducts(pageable));
        log.info("Список всех товаров успешно возвращен администратору. Количество элементов: {}", result.getTotalElements());
        return result;
    }

    @Operation(summary = "Одобрить товар", description = "Одобряет товар для продажи (требуется роль ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Товар успешно одобрен"),
            @ApiResponse(responseCode = "404", description = "Товар не найден"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approveProduct(@PathVariable Long id) {
        log.info("Получен запрос администратора на одобрение товара с ID: {}", id);
        productService.approveProduct(id);
        log.info("Товар с ID {} успешно одобрен администратором", id);
        return ResponseEntity.ok("Товар успешно одобрен");
    }
}
