package com.blpsteam.blpslab1.repositories.product;

import com.blpsteam.blpslab1.data.entities.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBrandAndNameAndDescriptionAndSellerId(String brand, String name, String description, Long sellerId);
    Page<Product> findByApproved(Boolean approved, Pageable pageable);
    Page<Product> findByApprovedAndNameContainingIgnoreCase( Boolean approved, String name, Pageable pageable);
}
