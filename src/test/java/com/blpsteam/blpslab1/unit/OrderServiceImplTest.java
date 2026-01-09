package com.blpsteam.blpslab1.unit;

import com.blpsteam.blpslab1.data.entities.core.Cart;
import com.blpsteam.blpslab1.data.entities.core.CartItem;
import com.blpsteam.blpslab1.data.entities.core.Order;
import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.OrderStatus;
import com.blpsteam.blpslab1.exceptions.impl.CartAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.CartItemAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.OrderPaymentException;
import com.blpsteam.blpslab1.exceptions.impl.UserAbsenceException;
import com.blpsteam.blpslab1.repositories.core.CartRepository;
import com.blpsteam.blpslab1.repositories.core.OrderRepository;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.service.CartService;
import com.blpsteam.blpslab1.service.PaymentService;
import com.blpsteam.blpslab1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private UserService userService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Cart testCart;
    private CartItem testCartItem;
    private static final Long USER_ID = 1L;
    private static final String PAYMENT_LINK = "https://yoomoney.ru/checkout/payments/v2/contract?orderId";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setUsername("testuser");

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setTotalPrice(5000L);

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setQuantity(2);
        testCartItem.setTotalPrice(5000);
        
        Set<CartItem> items = new HashSet<>();
        items.add(testCartItem);
        testCart.setItems(items);
    }

    @Test
    void testCreateOrder_Success() {
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(paymentService.createPayment(anyLong(), anyLong())).thenReturn(PAYMENT_LINK);

        String result = orderService.createOrder();

        assertNotNull(result);
        assertEquals(PAYMENT_LINK, result);

    }

    @Test
    void testCreateOrder_UserNotFound() {
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserAbsenceException.class, () -> {
            orderService.createOrder();
        });
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_UnpaidOrderExists() {
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(true);

        assertThrows(OrderPaymentException.class, () -> {
            orderService.createOrder();
        });
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_CartNotFound() {
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(CartAbsenceException.class, () -> {
            orderService.createOrder();
        });
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_EmptyCart() {
        Cart emptyCart = new Cart();
        emptyCart.setId(1L);
        emptyCart.setUser(testUser);
        emptyCart.setItems(new HashSet<>());

        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(emptyCart));

        assertThrows(CartItemAbsenceException.class, () -> {
            orderService.createOrder();
        });
        verify(orderRepository, never()).save(any(Order.class));
    }
}

