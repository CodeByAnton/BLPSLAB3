package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.CartResponseDTO;
import com.blpsteam.blpslab1.mapper.CartMapper;
import com.blpsteam.blpslab1.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyers/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @PreAuthorize("hasRole('BUYER')")
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart() {
        return ResponseEntity.ok(CartMapper.toDto(cartService.getCart()));
    }

    @PreAuthorize("hasRole('BUYER')")
    @DeleteMapping
    public ResponseEntity<CartResponseDTO> clearCart() {
        return ResponseEntity.ok(CartMapper.toDto(cartService.clearCart()));
    }

    @PreAuthorize("hasRole('BUYER')")
    @PostMapping
    public ResponseEntity<CartResponseDTO> createCart() {
        return ResponseEntity.ok(CartMapper.toDto(cartService.createCart()));
    }
}
