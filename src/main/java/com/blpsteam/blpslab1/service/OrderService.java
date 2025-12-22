package com.blpsteam.blpslab1.service;

import com.blpsteam.blpslab1.data.entities.core.Order;

import java.util.List;

public interface OrderService {
    String createOrder();
    void confirmPayment(String yooKassaPaymentResponse);
    void sendPaymentReminders(List<Order> orders);
}
