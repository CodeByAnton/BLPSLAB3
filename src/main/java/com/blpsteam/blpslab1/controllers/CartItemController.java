package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.CartItemQuantityRequestDTO;
import com.blpsteam.blpslab1.dto.CartItemRequestDTO;
import com.blpsteam.blpslab1.dto.CartItemResponseDTO;
import com.blpsteam.blpslab1.mapper.CartItemMapper;
import com.blpsteam.blpslab1.service.CartItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/buyers/carts/items")
@Tag(name = "Элементы корзины", description = "API для работы с элементами корзины")
public class CartItemController {

    @Autowired
    private CartItemService cartItemService;

    @Operation(summary = "Получить элемент корзины", description = "Возвращает элемент корзины по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элемент корзины успешно получен"),
            @ApiResponse(responseCode = "404", description = "Элемент корзины не найден"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BUYER')")
    public CartItemResponseDTO getCartItemById(@PathVariable Long id) {
        log.info("Получен запрос на получение элемента корзины с ID: {}", id);
        CartItemResponseDTO item = CartItemMapper.toDto(cartItemService.getCartItemById(id));
        log.info("Элемент корзины с ID {} успешно возвращен", id);
        return item;
    }

    @Operation(summary = "Получить все элементы корзины", description = "Возвращает все элементы корзины покупателя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список элементов корзины успешно получен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    @PreAuthorize("hasRole('BUYER')")
    public Page<CartItemResponseDTO> getAllCartItems(Pageable pageable) {
        log.info("Получен запрос на получение всех элементов корзины");
        Page<CartItemResponseDTO> items = CartItemMapper.toDtoPage(cartItemService.getAllCartItems(pageable));
        log.info("Список элементов корзины успешно возвращен. Количество элементов: {}", items.getTotalElements());
        return items;
    }

    @Operation(summary = "Добавить товар в корзину", description = "Добавляет товар в корзину покупателя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Товар успешно добавлен в корзину"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или недостаточно товара"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BUYER')")
    public CartItemResponseDTO createCartItem(@RequestBody CartItemRequestDTO cartItemRequestDTO) {
        log.info("Получен запрос на добавление товара в корзину. Товар ID: {}, количество: {}", 
                cartItemRequestDTO.productId(), cartItemRequestDTO.quantity());
        CartItemResponseDTO item = CartItemMapper.toDto(cartItemService.createCartItem(
                cartItemRequestDTO.productId(),
                cartItemRequestDTO.quantity()
        ));
        log.info("Товар успешно добавлен в корзину. Элемент корзины ID: {}", item.id());
        return item;
    }

    @Operation(summary = "Обновить элемент корзины", description = "Обновляет количество товара в корзине")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элемент корзины успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или недостаточно товара"),
            @ApiResponse(responseCode = "404", description = "Элемент корзины не найден"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUYER')")
    public CartItemResponseDTO updateCartItem(@PathVariable Long id, @RequestBody CartItemQuantityRequestDTO cartItemQuantityRequestDTO) {
        log.info("Получен запрос на обновление элемента корзины. Элемент ID: {}, новое количество: {}", 
                id, cartItemQuantityRequestDTO.quantity());
        CartItemResponseDTO item = CartItemMapper.toDto(cartItemService.updateCartItem(id, cartItemQuantityRequestDTO.quantity()));
        log.info("Элемент корзины с ID {} успешно обновлен", id);
        return item;
    }

    @Operation(summary = "Удалить элемент корзины", description = "Удаляет товар из корзины")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элемент корзины успешно удален"),
            @ApiResponse(responseCode = "404", description = "Элемент корзины не найден"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет доступа")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUYER')")
    public void deleteCartItem(@PathVariable Long id) {
        log.info("Получен запрос на удаление элемента корзины с ID: {}", id);
        cartItemService.deleteCartItemById(id);
        log.info("Элемент корзины с ID {} успешно удален", id);
    }
}
