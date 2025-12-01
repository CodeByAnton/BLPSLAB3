package com.blpsteam.blpslab1.dto;

public record CartItemResponseDTO(Long id,int quantity, int unitPrice,
                                  int totalPrice, Long cartId,
                                  Long productId) {
}
