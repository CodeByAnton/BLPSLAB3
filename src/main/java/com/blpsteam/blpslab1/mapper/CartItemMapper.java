package com.blpsteam.blpslab1.mapper;

import com.blpsteam.blpslab1.data.entities.core.CartItem;
import com.blpsteam.blpslab1.dto.CartItemResponseDTO;
import org.springframework.data.domain.Page;

public class CartItemMapper {
    
    public static CartItemResponseDTO toDto(CartItem cartItem) {
        return new CartItemResponseDTO(
                cartItem.getId(),
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                cartItem.getTotalPrice(),
                cartItem.getCart().getId(),
                cartItem.getProductId()
        );
    }
    
    public static Page<CartItemResponseDTO> toDtoPage(Page<CartItem> cartItems) {
        return cartItems.map(CartItemMapper::toDto);
    }
}

