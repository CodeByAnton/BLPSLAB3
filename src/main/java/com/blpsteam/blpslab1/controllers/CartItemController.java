package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.CartItemQuantityRequestDTO;
import com.blpsteam.blpslab1.dto.CartItemRequestDTO;
import com.blpsteam.blpslab1.dto.CartItemResponseDTO;
import com.blpsteam.blpslab1.service.CartItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyer/cartItems")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BUYER')")
    public CartItemResponseDTO getCartItemById(@PathVariable Long id) {
        return cartItemService.getCartItemById(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('BUYER')")
    public Page<CartItemResponseDTO> getAllCartItems(Pageable pageable) {
        return cartItemService.getAllCartItems(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BUYER')")
    public CartItemResponseDTO createCartItem(@RequestBody CartItemRequestDTO cartItemRequestDTO) {
        return cartItemService.createCartItem(cartItemRequestDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BUYER')")
    public CartItemResponseDTO updateCartItem(@PathVariable Long id, @RequestBody CartItemQuantityRequestDTO cartItemQuantityRequestDTO) {
        return cartItemService.updateCartItem(id, cartItemQuantityRequestDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BUYER')")
    public void deleteCartItem(@PathVariable Long id) {
        cartItemService.deleteCartItemById(id);
    }
}
