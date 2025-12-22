package com.blpsteam.blpslab1.util;

import com.blpsteam.blpslab1.data.entities.core.Order;
import com.blpsteam.blpslab1.data.enums.OrderStatus;
import com.blpsteam.blpslab1.repositories.core.OrderRepository;
import com.blpsteam.blpslab1.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class OrderReminderScheduler {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void checkUnpaidOrdersAndSendReminders() {
        List<Order> ordersToRemind = orderRepository
                .findByStatusAndReminderSentFalse(OrderStatus.UNPAID);

        try {
            orderService.sendPaymentReminders(ordersToRemind);
            for (Order order : ordersToRemind) {
                order.setReminderSent(true);
            }
        } catch (Exception e) {
            for (Order order : ordersToRemind) {
                log.warn("Failed to send reminder for order {}: {}", order.getId(), e.getMessage());
            }
        }
    }
}