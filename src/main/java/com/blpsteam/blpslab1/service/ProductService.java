package com.blpsteam.blpslab1.service;

import com.blpsteam.blpslab1.data.entities.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Product getProductById(Long id);
    Page<Product> getAllProducts(Pageable pageable);
    Page<Product> getApprovedProducts(String name, Pageable pageable);
    Product addProduct(String brand, String name, String description, int quantity, Long price, Double averageRating, int reviewsCount);
    boolean approveProduct(Long productId);
}