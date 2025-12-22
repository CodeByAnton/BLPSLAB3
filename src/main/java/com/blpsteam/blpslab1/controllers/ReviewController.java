package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.ReviewRequestDTO;
import com.blpsteam.blpslab1.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PreAuthorize("hasRole('BUYER')")
    @PostMapping
    public ResponseEntity<String> addReview(@PathVariable Long productId, @RequestBody ReviewRequestDTO dto) {
        reviewService.publishReview(productId, dto.rating());
        return ResponseEntity.ok("Отзыв отправлен на обработку");
    }
}
