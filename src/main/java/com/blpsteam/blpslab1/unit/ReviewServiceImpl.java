package com.blpsteam.blpslab1.unit;

import com.blpsteam.blpslab1.dto.ReviewRequestDTO;
import com.blpsteam.blpslab1.exceptions.impl.ReviewDataException;
import com.blpsteam.blpslab1.exceptions.impl.ReviewAbsenceException;
import com.blpsteam.blpslab1.repositories.core.ReviewRepository;
import com.blpsteam.blpslab1.repositories.product.ProductRepository;
import com.blpsteam.blpslab1.service.ReviewService;
import com.blpsteam.blpslab1.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductRepository productRepository;

    @Override
    public void publishReview(Long productId, int rating) {
        log.info("Попытка публикации отзыва. Товар ID: {}, рейтинг: {}", productId, rating);
        
        if (!productRepository.existsById(productId)){
            log.warn("Попытка публикации отзыва на несуществующий товар ID: {}", productId);
            throw new ReviewAbsenceException("Товар не существует");
        }
        
        Long userId = userService.getUserIdFromContext();
        if (reviewRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            log.warn("Попытка публикации повторного отзыва. Пользователь ID: {}, товар ID: {}", userId, productId);
            throw new ReviewAbsenceException("Ваш отзыв на этот товар уже существует");
        }
        
        if (rating < 0 || rating > 5) {
            log.warn("Попытка публикации отзыва с некорректным рейтингом: {}. Товар ID: {}", rating, productId);
            throw new ReviewDataException("Рейтинг должен быть от 0 до 5");
        }
        
        ReviewRequestDTO reviewDTO = new ReviewRequestDTO(productId, userId, rating);
        messagingTemplate.convertAndSend("/queue/reviews", reviewDTO);
        log.info("Отзыв успешно отправлен на обработку. Товар ID: {}, пользователь ID: {}, рейтинг: {}", 
                productId, userId, rating);
    }
}