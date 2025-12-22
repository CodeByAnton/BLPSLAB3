package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.Cart;
import com.blpsteam.blpslab1.data.entities.core.Order;
import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.OrderStatus;
import com.blpsteam.blpslab1.exceptions.impl.OrderPaymentException;
import com.blpsteam.blpslab1.exceptions.impl.CartItemAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.OrderAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.UserAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.CartAbsenceException;
import com.blpsteam.blpslab1.service.PaymentService;
import com.blpsteam.blpslab1.repositories.core.CartRepository;
import com.blpsteam.blpslab1.repositories.core.OrderRepository;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.service.CartService;
import com.blpsteam.blpslab1.service.OrderService;
import com.blpsteam.blpslab1.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PaymentService paymentService;
    @Override
    @Transactional
    public String createOrder() {
        log.info("CreateOrder method");
        Long userId = userService.getUserIdFromContext();
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserAbsenceException("Пользователь не найден"));

        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)) {
            throw new OrderPaymentException("У пользователя уже есть неоплаченный заказ");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartAbsenceException("Корзина для пользователя с id " + userId + " не найдена"));

        if (cart.getItems().isEmpty()) {
            throw new CartItemAbsenceException("Корзина пуста");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(cart.getTotalPrice());
        order.setStatus(OrderStatus.UNPAID);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        return paymentService.createPayment(order.getTotalPrice(), order.getId());
    }

    @Override
    @Transactional
    public void confirmPayment(String yooKassaPaymentResponse){
        log.info("ConfirmPayment");
        log.info(yooKassaPaymentResponse);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(yooKassaPaymentResponse);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }

        String event = root.path("event").asText();
        log.info("event = {}", event);
        String status = root.path("object").path("status").asText();
        log.info("status = {}", status);
        String orderIdStr = root.path("object").path("metadata").path("order_id").asText();
        Long orderId = Long.parseLong(orderIdStr);
        log.info("order_id = {}", orderId);

        if ("payment.succeeded".equals(event) && "succeeded".equals(status)) {
            log.info("Payment succeeded");
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderAbsenceException("Заказ не найден"));
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            Long userId = order.getUser().getId();
            cartService.clearCartAfterPayment(userId);
            log.info("userId = {}", userId);
        }
    }

    public void sendPaymentReminders(List<Order> orders) {
        for (Order order : orders) {
            log.info("Sending payment reminder to user {} for order {}", order.getUser().getId(), order.getId());
        }
    }
}
