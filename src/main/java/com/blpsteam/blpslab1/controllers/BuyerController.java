package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.ProductResponseDTO;
import com.blpsteam.blpslab1.mapper.ProductMapper;
import com.blpsteam.blpslab1.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyers")
public class BuyerController {

    @Autowired
    private ProductService productService;

    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/catalog")
    public Page<ProductResponseDTO> getCatalog(@RequestParam(required = false) String name,
                                               Pageable pageable) {
        return ProductMapper.toDtoPage(productService.getApprovedProducts(name, pageable));
    }
}

