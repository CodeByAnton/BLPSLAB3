package com.blpsteam.blpslab1.service;

import com.blpsteam.blpslab1.data.entities.product.Product;
import com.blpsteam.blpslab1.dto.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface ProductService {
    ProductResponseDTO getProductById(Long id);
    Page<ProductResponseDTO> getAllProducts(Pageable pageable);
    Page<ProductResponseDTO> getApprovedProducts(String name, Pageable pageable);
    Product addProduct(String brand, String name, String description, int quantity, Long price, Double averageRating, int reviewsCount);
    boolean approveProduct(Long productId);
}