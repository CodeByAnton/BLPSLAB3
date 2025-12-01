package com.blpsteam.blpslab1.controllers;

import com.blpsteam.blpslab1.dto.ReviewRequestDTO;
import com.blpsteam.blpslab1.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PreAuthorize("hasRole('BUYER')")
    @PostMapping
    public ResponseEntity<String> addReview(@RequestBody ReviewRequestDTO dto) {
        reviewService.publishReview(dto);
        return ResponseEntity.ok("Review submitted for processing");
    }
}
