package com.blpsteam.blpslab1.mapper;

import com.blpsteam.blpslab1.data.entities.product.Product;
import com.blpsteam.blpslab1.dto.ProductResponseDTO;
import org.springframework.data.domain.Page;

public class ProductMapper {
    
    public static ProductResponseDTO toDto(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getBrand(),
                product.getName(),
                product.getDescription(),
                product.getQuantity(),
                product.getPrice(),
                product.getApproved(),
                product.getAverageRating(),
                product.getReviewCount()
        );
    }
    
    public static Page<ProductResponseDTO> toDtoPage(Page<Product> products) {
        return products.map(ProductMapper::toDto);
    }
}

