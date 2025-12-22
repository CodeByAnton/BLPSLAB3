package com.blpsteam.blpslab1.mapper;

import com.blpsteam.blpslab1.data.entities.core.Cart;
import com.blpsteam.blpslab1.dto.CartResponseDTO;

public class CartMapper {
    
    public static CartResponseDTO toDto(Cart cart) {
        return new CartResponseDTO(cart.getId(), cart.getUser().getUsername());
    }
}

