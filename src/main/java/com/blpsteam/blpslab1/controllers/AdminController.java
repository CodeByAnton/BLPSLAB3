package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.ProductResponseDTO;
import com.blpsteam.blpslab1.mapper.ProductMapper;
import com.blpsteam.blpslab1.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminController {

    @Autowired
    private ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        return ProductMapper.toDtoPage(productService.getAllProducts(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approveProduct(@PathVariable Long id) {
        productService.approveProduct(id);
        return ResponseEntity.ok("Товар успешно одобрен");
    }
}
