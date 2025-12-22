package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.Cart;
import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.OrderStatus;
import com.blpsteam.blpslab1.exceptions.impl.CartAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.UserAbsenceException;
import com.blpsteam.blpslab1.repositories.core.CartRepository;
import com.blpsteam.blpslab1.repositories.core.OrderRepository;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.service.CartItemService;
import com.blpsteam.blpslab1.service.CartService;
import com.blpsteam.blpslab1.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CartItemService cartItemService;
    
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Cart getCart() {
        Long userId = userService.getUserIdFromContext();
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartAbsenceException("Корзина для пользователя с id " + userId + " не найдена"));
    }

   
    

    @Override
    @Transactional
    public Cart clearCart() {
        log.info("ClearCart method");
        Long userId = userService.getUserIdFromContext();

        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            throw new IllegalArgumentException("Нельзя очистить корзину, пока у вас есть неоплаченный заказ");
        }

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartAbsenceException("Корзина для пользователя с id " + userId + " не найдена"));

        cartItemService.clearCartAndUpdateProductQuantities(cart.getId());
        cart.getItems().clear();
        cart.setTotalPrice(0L);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart createCart() {
        log.info("CreateCart method");
        Long userId = userService.getUserIdFromContext();
        if (cartRepository.findByUserId(userId).isPresent()) {
            throw new CartAbsenceException("У вас уже есть корзина");
        }
        Cart cart = new Cart();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserAbsenceException("Пользователь с таким id не найден"));
        cart.setUser(user);
        log.info("Cart has been created for user {}", userId);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCartAfterPayment(Long userId) {
        log.info("ClearCartAfterPayment method");
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            throw new IllegalArgumentException("Нельзя очистить корзину, пока у вас есть неоплаченный заказ");
        }

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartAbsenceException("Корзина для пользователя с id= " + userId + " не найдена"));

        cart.getItems().clear();
        cart.setTotalPrice(0L);
        cartRepository.save(cart);
        log.info("Cart has been cleared after payment for user {}", userId);
    }
}

