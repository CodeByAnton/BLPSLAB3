package com.blpsteam.blpslab1.unit;

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
        log.info("Попытка создания заказа");
        Long userId = userService.getUserIdFromContext();
        log.info("Создание заказа для пользователя с ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(()-> {
                    log.warn("Попытка создания заказа несуществующим пользователем с ID: {}", userId);
                    return new UserAbsenceException("Пользователь не найден");
                });

        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)) {
            log.warn("Попытка создания заказа при наличии неоплаченного заказа. Пользователь ID: {}", userId);
            throw new OrderPaymentException("У пользователя уже есть неоплаченный заказ");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка создания заказа без корзины. Пользователь ID: {}", userId);
                    return new CartAbsenceException("Корзина для пользователя с id " + userId + " не найдена");
                });

        if (cart.getItems().isEmpty()) {
            log.warn("Попытка создания заказа с пустой корзиной. Пользователь ID: {}", userId);
            throw new CartItemAbsenceException("Корзина пуста");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(cart.getTotalPrice());
        order.setStatus(OrderStatus.UNPAID);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);
        log.info("Заказ успешно создан. ID заказа: {}, сумма: {} руб., пользователь ID: {}", 
                order.getId(), order.getTotalPrice(), userId);

        String paymentLink = paymentService.createPayment(order.getTotalPrice(), order.getId());
        log.info("Ссылка на оплату для заказа ID: {} успешно сгенерирована", order.getId());
        return paymentLink;
    }

    @Override
    @Transactional
    public void confirmPayment(String yooKassaPaymentResponse){
        log.info("Получено уведомление от YooKassa о статусе платежа");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(yooKassaPaymentResponse);
        } catch (Exception e) {
            log.error("Ошибка при парсинге уведомления от YooKassa: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        String event = root.path("event").asText();
        log.info("Событие от YooKassa: {}", event);
        String status = root.path("object").path("status").asText();
        log.info("Статус платежа: {}", status);
        String orderIdStr = root.path("object").path("metadata").path("order_id").asText();
        Long orderId = Long.parseLong(orderIdStr);
        log.info("ID заказа из уведомления: {}", orderId);

        if ("payment.succeeded".equals(event) && "succeeded".equals(status)) {
            log.info("Платеж успешно обработан. Заказ ID: {}", orderId);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Заказ с ID {} не найден при обработке платежа", orderId);
                        return new OrderAbsenceException("Заказ не найден");
                    });
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            Long userId = order.getUser().getId();
            log.info("Статус заказа ID: {} изменен на PAID. Очистка корзины пользователя ID: {}", orderId, userId);
            cartService.clearCartAfterPayment(userId);
            log.info("Платеж успешно подтвержден. Заказ ID: {}, пользователь ID: {}", orderId, userId);
        } else {
            log.info("Платеж не подтвержден. Событие: {}, статус: {}, заказ ID: {}", event, status, orderId);
        }
    }

    public void sendPaymentReminders(List<Order> orders) {
        log.info("Отправка напоминаний о платеже для {} заказов", orders.size());
        for (Order order : orders) {
            log.info("Отправка напоминания о платеже пользователю ID: {} для заказа ID: {}", 
                    order.getUser().getId(), order.getId());
        }
        log.info("Напоминания о платеже успешно отправлены");
    }
}
