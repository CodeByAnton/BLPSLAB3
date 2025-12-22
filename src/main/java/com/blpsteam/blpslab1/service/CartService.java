package com.blpsteam.blpslab1.service;

import com.blpsteam.blpslab1.data.entities.core.Cart;

public interface CartService {
    Cart getCart();
    Cart clearCart();
    Cart createCart();
    void clearCartAfterPayment(Long userId);
}
