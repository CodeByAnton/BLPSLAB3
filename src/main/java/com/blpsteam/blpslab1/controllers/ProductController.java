package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.data.entities.product.Product;
import com.blpsteam.blpslab1.dto.ProductRequestDTO;
import com.blpsteam.blpslab1.dto.ProductResponseDTO;
import com.blpsteam.blpslab1.mapper.ProductMapper;
import com.blpsteam.blpslab1.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PreAuthorize("hasRole('BUYER')")
    @GetMapping
    public Page<ProductResponseDTO> getCatalog(@RequestParam(required = false) String name,
                                               Pageable pageable) {
        return ProductMapper.toDtoPage(productService.getApprovedProducts(name, pageable));
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping
    public ResponseEntity<String> addProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        Product product = productService.addProduct(
                productRequestDTO.brand(),
                productRequestDTO.name(),
                productRequestDTO.description(),
                productRequestDTO.quantity(),
                productRequestDTO.price(),
                0d,
                0
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(String.format("Товар %s успешно добавлен", product.getName()));
    }
}

