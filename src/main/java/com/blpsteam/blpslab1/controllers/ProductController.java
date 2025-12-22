package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.data.entities.product.Product;
import com.blpsteam.blpslab1.dto.ProductRequestDTO;
import com.blpsteam.blpslab1.dto.ProductResponseDTO;
import com.blpsteam.blpslab1.mapper.ProductMapper;
import com.blpsteam.blpslab1.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Товары", description = "API для работы с товарами")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Получить каталог товаров", description = "Возвращает список одобренных товаров с возможностью поиска")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список товаров успешно получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('BUYER')")
    @GetMapping
    public Page<ProductResponseDTO> getCatalog(@RequestParam(required = false) String name,
                                               Pageable pageable) {
        log.info("Получен запрос на получение каталога товаров. Поиск: {}", name);
        Page<ProductResponseDTO> result = ProductMapper.toDtoPage(productService.getApprovedProducts(name, pageable));
        log.info("Каталог товаров успешно возвращен. Количество элементов: {}", result.getTotalElements());
        return result;
    }

    @Operation(summary = "Добавить товар", description = "Добавляет новый товар (требуется роль SELLER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Товар успешно добавлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        log.info("Получен запрос на добавление товара: {}", productRequestDTO.name());
        Product product = productService.addProduct(
                productRequestDTO.brand(),
                productRequestDTO.name(),
                productRequestDTO.description(),
                productRequestDTO.quantity(),
                productRequestDTO.price(),
                0d,
                0
        );
        log.info("Товар успешно добавлен через API: {} (ID: {})", product.getName(), product.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(String.format("Товар %s успешно добавлен", product.getName()));
    }
}

