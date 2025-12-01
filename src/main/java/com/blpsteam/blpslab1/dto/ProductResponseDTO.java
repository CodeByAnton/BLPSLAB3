package com.blpsteam.blpslab1.dto;

public record ProductResponseDTO(Long id, String brand, String name,
                                 String description, int quantity,
                                 Long price, boolean isApproved,
                                 Double averageRating, int reviewsCount) {
}
