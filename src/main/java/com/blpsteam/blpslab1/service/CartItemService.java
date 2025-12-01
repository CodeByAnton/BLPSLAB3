package com.blpsteam.blpslab1.service;

import com.blpsteam.blpslab1.dto.CartItemQuantityRequestDTO;
import com.blpsteam.blpslab1.dto.CartItemRequestDTO;
import com.blpsteam.blpslab1.dto.CartItemResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartItemService {
    CartItemResponseDTO getCartItemById(Long id);
    Page<CartItemResponseDTO> getAllCartItems(Pageable pageable);
    CartItemResponseDTO createCartItem(CartItemRequestDTO cartItemRequestDTO);
    CartItemResponseDTO updateCartItem(Long id, CartItemQuantityRequestDTO cartItemRequestDTO);
    void deleteCartItemById(Long id);
    void clearCartAndUpdateProductQuantities(Long cartId);
}
