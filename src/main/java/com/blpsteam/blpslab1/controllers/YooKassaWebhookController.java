package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.service.OrderService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/yookassa/notifications")
@Hidden
public class YooKassaWebhookController {

    @Autowired
    private OrderService orderService;


    @PostMapping
    public ResponseEntity<String> handlePayment(@RequestBody String payload) {
        log.info("Получено уведомление от YooKassa о статусе платежа");
        orderService.confirmPayment(payload);
        log.info("Уведомление от YooKassa успешно обработано");
        return ResponseEntity.ok("OK");
    }
}
