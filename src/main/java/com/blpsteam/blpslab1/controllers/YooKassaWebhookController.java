package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/yookassa/notifications")
public class YooKassaWebhookController {

    private final OrderService orderService;

    public YooKassaWebhookController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> handlePayment(@RequestBody String payload) {
        orderService.confirmPayment(payload);
        return ResponseEntity.ok().body("OK");

    }
}
