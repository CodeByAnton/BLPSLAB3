package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.BalanceIncreaseRequestDTO;
import com.blpsteam.blpslab1.dto.ProductResponseDTO;
import com.blpsteam.blpslab1.service.BuyerService;
import com.blpsteam.blpslab1.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController()
@RequestMapping("/api/v1/buyer")
public class BuyerController {
    private final ProductService productService;
    private final BuyerService buyerService;
    public BuyerController(ProductService productService, BuyerService buyerService) {
        this.productService = productService;
        this.buyerService = buyerService;

    }

    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/catalog")
    public Page<ProductResponseDTO> getCatalog(@RequestParam(required = false) String name,
                                               Pageable pageable) {
        return productService.getApprovedProducts(name,pageable);

    }

    @PreAuthorize("hasRole('BUYER')")
    @PutMapping("/balance")
    public ResponseEntity<String> increaseBalance(@RequestBody BalanceIncreaseRequestDTO balanceIncreaseRequestDTO, Principal principal) {
        buyerService.increaseBalance(principal.getName(), balanceIncreaseRequestDTO.amount());
        return ResponseEntity.ok("Balance successfully increased by " + balanceIncreaseRequestDTO.amount());
    }
}
