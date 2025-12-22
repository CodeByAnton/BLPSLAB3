package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.OrderResponseDTO;
import com.blpsteam.blpslab1.mapper.OrderMapper;
import com.blpsteam.blpslab1.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/buyers/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PreAuthorize("hasRole('BUYER')")
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder() {
        String paymentLink = orderService.createOrder();
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderMapper.toDto(paymentLink));
    }
}
