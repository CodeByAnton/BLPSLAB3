package com.blpsteam.blpslab1.unit;

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
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));

        Cart result = cartService.getCart();

        assertNotNull(result);
        assertEquals(testCart.getId(), result.getId());
        verify(cartRepository).findByUserId(USER_ID);
    }

    @Test
    void testGetCart_NotFound() {
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(CartAbsenceException.class, () -> {
            cartService.getCart();
        });
    }

    @Test
    void testClearCart_Success() {
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.clearCart();

        assertNotNull(result);
        verify(cartItemService).clearCartAndUpdateProductQuantities(testCart.getId());
        verify(cartRepository).save(testCart);
    }

    @Test
    void testClearCart_UnpaidOrderExists() {
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            cartService.clearCart();
        });
        verify(cartItemService, never()).clearCartAndUpdateProductQuantities(anyLong());
    }

    @Test
    void testCreateCart_Success() {
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.createCart();

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testCreateCart_AlreadyExists() {
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));

        assertThrows(CartAbsenceException.class, () -> {
            cartService.createCart();
        });
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testCreateCart_UserNotFound() {
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserAbsenceException.class, () -> {
            cartService.createCart();
        });
    }

    @Test
    void testClearCartAfterPayment_Success() {
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        cartService.clearCartAfterPayment(USER_ID);

        verify(cartRepository).save(testCart);
    }

    @Test
    void testClearCartAfterPayment_UnpaidOrderExists() {
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            cartService.clearCartAfterPayment(USER_ID);
        });
    }
}

