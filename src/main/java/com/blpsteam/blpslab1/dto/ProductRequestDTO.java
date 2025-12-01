package com.blpsteam.blpslab1.dto;

public record ProductRequestDTO(String brand, String name,
                                String description, int quantity,
                                Long price) {
}
