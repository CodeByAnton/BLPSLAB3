package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.data.entities.core.Cart;
import com.blpsteam.blpslab1.dto.CartResponseDTO;
import com.blpsteam.blpslab1.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/buyer/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PreAuthorize("hasRole('BUYER')")
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart() {
        Cart cart = cartService.getCart();
        return ResponseEntity.ok(new CartResponseDTO(cart.getId(), cart.getUser().getUsername()));
    }

    @PreAuthorize("hasRole('BUYER')")
    @DeleteMapping
    public ResponseEntity<CartResponseDTO> clearCart() {
        Cart cart = cartService.getCart();
        cartService.clearCart();
        return ResponseEntity.ok(new CartResponseDTO(cart.getId(), cart.getUser().getUsername()));
    }

    @PreAuthorize("hasRole('BUYER')")
    @PostMapping
    public ResponseEntity<CartResponseDTO> createCart() {
        Cart cart = cartService.createCart();
        return ResponseEntity.ok(new CartResponseDTO(cart.getId(), cart.getUser().getUsername()));
    }
}
