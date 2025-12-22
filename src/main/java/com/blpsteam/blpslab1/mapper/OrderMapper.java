package com.blpsteam.blpslab1.mapper;

import com.blpsteam.blpslab1.dto.OrderResponseDTO;

public class OrderMapper {
    
    public static OrderResponseDTO toDto(String paymentLink) {
        return new OrderResponseDTO(paymentLink);
    }
}

