package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.dto.ReviewRequestDTO;
import com.blpsteam.blpslab1.exceptions.ReviewDataException;
import com.blpsteam.blpslab1.exceptions.impl.ReviewAbsenceException;
import com.blpsteam.blpslab1.repositories.core.ReviewRepository;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.repositories.product.ProductRepository;
import com.blpsteam.blpslab1.service.ReviewService;
import com.blpsteam.blpslab1.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ProductRepository productRepository;

    public ReviewServiceImpl(SimpMessagingTemplate messagingTemplate, ReviewRepository reviewRepository,
                             UserService userService, ProductRepository productRepository) {
        this.messagingTemplate = messagingTemplate;
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.productRepository = productRepository;
    }

    @Override
    public void publishReview(ReviewRequestDTO reviewDTO) {
        if (!productRepository.existsById(reviewDTO.productId())){
            throw new ReviewAbsenceException("Product does not exist");
        }
        Long userId=userService.getUserIdFromContext();
        if (reviewRepository.findByUserIdAndProductId(userId,reviewDTO.productId()).isPresent()) {
            throw new ReviewAbsenceException("Ur review on this product already exists");
        }
        if (reviewDTO.rating() < 0 || reviewDTO.rating() > 5) {
            throw new ReviewDataException("Rating must be between 0 and 5");
        }
        ReviewRequestDTO fullDTO=new ReviewRequestDTO(reviewDTO.productId(),userId,reviewDTO.rating());
        messagingTemplate.convertAndSend("/queue/reviews", fullDTO);
    }
}