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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/buyers")
@Tag(name = "Покупатели", description = "API для покупателей")
public class BuyerController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Получить каталог товаров", description = "Возвращает список одобренных товаров для покупателей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Каталог успешно получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/catalog")
    public Page<ProductResponseDTO> getCatalog(@RequestParam(required = false) String name,
                                               Pageable pageable) {
        log.info("Получен запрос на получение каталога товаров для покупателя. Поиск: {}", name);
        Page<ProductResponseDTO> result = ProductMapper.toDtoPage(productService.getApprovedProducts(name, pageable));
        log.info("Каталог товаров успешно возвращен покупателю. Количество элементов: {}", result.getTotalElements());
        return result;
    }
}

