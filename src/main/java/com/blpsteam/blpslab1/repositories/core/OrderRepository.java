package com.blpsteam.blpslab1.repositories.core;

import com.blpsteam.blpslab1.data.entities.core.Order;
import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
    boolean existsByUserIdAndStatus(Long userId, OrderStatus status);
    Optional<Order> findByUserAndStatus(User buyer,OrderStatus status);
    @EntityGraph(attributePaths = {"user"})
    List<Order> findByStatusAndReminderSentFalse(OrderStatus status);


}
