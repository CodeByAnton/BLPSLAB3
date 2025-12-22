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
        log.info("Получение корзины для пользователя ID: {}", userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Корзина для пользователя ID: {} не найдена", userId);
                    return new CartAbsenceException("Корзина для пользователя с id " + userId + " не найдена");
                });
        log.info("Корзина успешно получена. Пользователь ID: {}, количество элементов: {}", 
                userId, cart.getItems().size());
        return cart;
    }

   
    

    @Override
    @Transactional
    public Cart clearCart() {
        Long userId = userService.getUserIdFromContext();
        log.info("Попытка очистки корзины для пользователя ID: {}", userId);

        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            log.warn("Попытка очистки корзины при наличии неоплаченного заказа. Пользователь ID: {}", userId);
            throw new IllegalArgumentException("Нельзя очистить корзину, пока у вас есть неоплаченный заказ");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка очистки несуществующей корзины. Пользователь ID: {}", userId);
                    return new CartAbsenceException("Корзина для пользователя с id " + userId + " не найдена");
                });

        int itemsCount = cart.getItems().size();
        cartItemService.clearCartAndUpdateProductQuantities(cart.getId());
        cart.getItems().clear();
        cart.setTotalPrice(0L);
        Cart saved = cartRepository.save(cart);
        log.info("Корзина успешно очищена. Пользователь ID: {}, удалено элементов: {}", userId, itemsCount);
        return saved;
    }

    @Override
    @Transactional
    public Cart createCart() {
        Long userId = userService.getUserIdFromContext();
        log.info("Попытка создания корзины для пользователя ID: {}", userId);
        
        if (cartRepository.findByUserId(userId).isPresent()) {
            log.warn("Попытка создания корзины при её наличии. Пользователь ID: {}", userId);
            throw new CartAbsenceException("У вас уже есть корзина");
        }
        
        Cart cart = new Cart();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка создания корзины несуществующим пользователем ID: {}", userId);
                    return new UserAbsenceException("Пользователь с таким id не найден");
                });
        cart.setUser(user);
        Cart saved = cartRepository.save(cart);
        log.info("Корзина успешно создана для пользователя ID: {}. ID корзины: {}", userId, saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void clearCartAfterPayment(Long userId) {
        log.info("Очистка корзины после оплаты для пользователя ID: {}", userId);
        
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            log.warn("Попытка очистки корзины после оплаты при наличии неоплаченного заказа. Пользователь ID: {}", userId);
            throw new IllegalArgumentException("Нельзя очистить корзину, пока у вас есть неоплаченный заказ");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.error("Корзина не найдена при очистке после оплаты. Пользователь ID: {}", userId);
                    return new CartAbsenceException("Корзина для пользователя с id= " + userId + " не найдена");
                });

        int itemsCount = cart.getItems().size();
        cart.getItems().clear();
        cart.setTotalPrice(0L);
        cartRepository.save(cart);
        log.info("Корзина успешно очищена после оплаты. Пользователь ID: {}, удалено элементов: {}", userId, itemsCount);
    }
}

