package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.Cart;
import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.OrderStatus;
import com.blpsteam.blpslab1.exceptions.impl.CartAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.UserAbsenceException;
import com.blpsteam.blpslab1.repositories.core.CartRepository;
import com.blpsteam.blpslab1.repositories.core.OrderRepository;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.service.CartItemService;
import com.blpsteam.blpslab1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private CartItemService cartItemService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;
    private User testUser;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setUsername("testuser");

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setTotalPrice(0L);
    }

    @Test
    void testGetCart_Success() {
        // Arrange
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));

        // Act
        Cart result = cartService.getCart();

        // Assert
        assertNotNull(result);
        assertEquals(testCart.getId(), result.getId());
        verify(cartRepository).findByUserId(USER_ID);
    }

    @Test
    void testGetCart_NotFound() {
        // Arrange
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartAbsenceException.class, () -> {
            cartService.getCart();
        });
    }

    @Test
    void testClearCart_Success() {
        // Arrange
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.clearCart();

        // Assert
        assertNotNull(result);
        verify(cartItemService).clearCartAndUpdateProductQuantities(testCart.getId());
        verify(cartRepository).save(testCart);
    }

    @Test
    void testClearCart_UnpaidOrderExists() {
        // Arrange
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartService.clearCart();
        });
        verify(cartItemService, never()).clearCartAndUpdateProductQuantities(anyLong());
    }

    @Test
    void testCreateCart_Success() {
        // Arrange
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.createCart();

        // Assert
        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testCreateCart_AlreadyExists() {
        // Arrange
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));

        // Act & Assert
        assertThrows(CartAbsenceException.class, () -> {
            cartService.createCart();
        });
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testCreateCart_UserNotFound() {
        // Arrange
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserAbsenceException.class, () -> {
            cartService.createCart();
        });
    }

    @Test
    void testClearCartAfterPayment_Success() {
        // Arrange
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        cartService.clearCartAfterPayment(USER_ID);

        // Assert
        verify(cartRepository).save(testCart);
    }

    @Test
    void testClearCartAfterPayment_UnpaidOrderExists() {
        // Arrange
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartService.clearCartAfterPayment(USER_ID);
        });
    }
}

