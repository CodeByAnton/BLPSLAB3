package com.blpsteam.blpslab1.repositories.core;

import com.blpsteam.blpslab1.data.entities.core.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);
}
