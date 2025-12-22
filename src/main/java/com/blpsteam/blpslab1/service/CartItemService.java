package com.blpsteam.blpslab1.service;

import com.blpsteam.blpslab1.data.entities.core.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartItemService {
    CartItem getCartItemById(Long id);
    Page<CartItem> getAllCartItems(Pageable pageable);
    CartItem createCartItem(Long productId, int quantity);
    CartItem updateCartItem(Long id, int quantity);
    void deleteCartItemById(Long id);
    void clearCartAndUpdateProductQuantities(Long cartId);
}
