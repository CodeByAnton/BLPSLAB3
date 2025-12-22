package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.Cart;
import com.blpsteam.blpslab1.data.entities.core.CartItem;
import com.blpsteam.blpslab1.data.entities.product.Product;
import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.OrderStatus;
import com.blpsteam.blpslab1.exceptions.impl.CartAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.CartItemAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.CartItemQuantityException;
import com.blpsteam.blpslab1.exceptions.impl.ProductAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.UserAbsenceException;
import com.blpsteam.blpslab1.repositories.core.CartItemRepository;
import com.blpsteam.blpslab1.repositories.core.CartRepository;
import com.blpsteam.blpslab1.repositories.core.OrderRepository;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.repositories.product.ProductRepository;
import com.blpsteam.blpslab1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartItemServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    private User testUser;
    private Cart testCart;
    private CartItem testCartItem;
    private Product testProduct;
    private static final Long USER_ID = 1L;
    private static final Long CART_ID = 1L;
    private static final Long PRODUCT_ID = 1L;
    private static final Long CART_ITEM_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setUsername("testuser");

        testCart = new Cart();
        testCart.setId(CART_ID);
        testCart.setUser(testUser);
        testCart.setTotalPrice(0L);
        testCart.setItems(new HashSet<>());

        testProduct = new Product();
        testProduct.setId(PRODUCT_ID);
        testProduct.setBrand("TestBrand");
        testProduct.setName("TestProduct");
        testProduct.setDescription("Test Description");
        testProduct.setQuantity(100);
        testProduct.setPrice(1000L);
        testProduct.setApproved(true);
        testProduct.setSellerId(2L);

        testCartItem = new CartItem();
        testCartItem.setId(CART_ITEM_ID);
        testCartItem.setProductId(PRODUCT_ID);
        testCartItem.setQuantity(2);
        testCartItem.setUnitPrice(1000);
        testCartItem.setTotalPrice(2000);
        testCartItem.setCart(testCart);
    }

    @Test
    void testGetCartItemById_Success() {
        // Arrange
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(testCartItem));

        // Act
        CartItem result = cartItemService.getCartItemById(CART_ITEM_ID);

        // Assert
        assertNotNull(result);
        assertEquals(CART_ITEM_ID, result.getId());
        verify(cartItemRepository).findById(CART_ITEM_ID);
    }

    @Test
    void testGetCartItemById_NotFound() {
        // Arrange
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartItemAbsenceException.class, () -> {
            cartItemService.getCartItemById(CART_ITEM_ID);
        });
    }

    @Test
    void testGetAllCartItems_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<CartItem> cartItems = List.of(testCartItem);
        
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(cartItems);

        // Act
        Page<CartItem> result = cartItemService.getAllCartItems(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cartRepository).findByUserId(USER_ID);
        verify(cartItemRepository).findByCartId(CART_ID);
    }

    @Test
    void testGetAllCartItems_CartNotFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartAbsenceException.class, () -> {
            cartItemService.getAllCartItems(pageable);
        });
    }

    @Test
    void testCreateCartItem_Success() {
        // Arrange
        int quantity = 5;
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(CART_ITEM_ID);
            return item;
        });
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartItem result = cartItemService.createCartItem(PRODUCT_ID, quantity);

        // Assert
        assertNotNull(result);
        assertEquals(quantity, result.getQuantity());
        assertEquals(PRODUCT_ID, result.getProductId());
        verify(productRepository).save(testProduct);
        verify(cartItemRepository).save(any(CartItem.class));
        verify(cartRepository).save(testCart);
    }

    @Test
    void testCreateCartItem_CartCreatedAutomatically() {
        // Arrange
        int quantity = 5;
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(CART_ID);
            return cart;
        });
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(CART_ITEM_ID);
            return item;
        });

        // Act
        CartItem result = cartItemService.createCartItem(PRODUCT_ID, quantity);

        // Assert
        assertNotNull(result);
        // Корзина сохраняется дважды: при создании и после добавления элемента
        verify(cartRepository, times(2)).save(any(Cart.class));
    }

    @Test
    void testCreateCartItem_UserNotFound() {
        // Arrange
        int quantity = 5;
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserAbsenceException.class, () -> {
            cartItemService.createCartItem(PRODUCT_ID, quantity);
        });
    }

    @Test
    void testCreateCartItem_InvalidQuantity_Zero() {

        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.createCartItem(PRODUCT_ID, 0);
        });
    }

    @Test
    void testCreateCartItem_InvalidQuantity_Negative() {
        // Act & Assert
        // Проверка quantity <= 0 происходит ПЕРВОЙ в методе, до любых других вызовов
        // Поэтому моки не нужны - исключение выбрасывается сразу
        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.createCartItem(PRODUCT_ID, -1);
        });
    }

    @Test
    void testCreateCartItem_UnpaidOrderExists() {
        // Arrange
        int quantity = 5;
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.createCartItem(PRODUCT_ID, quantity);
        });
        verify(productRepository, never()).save(any());
    }

    @Test
    void testCreateCartItem_ProductNotFound() {
        // Arrange
        int quantity = 5;
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductAbsenceException.class, () -> {
            cartItemService.createCartItem(PRODUCT_ID, quantity);
        });
    }

    @Test
    void testCreateCartItem_ProductNotApproved() {
        // Arrange
        int quantity = 5;
        testProduct.setApproved(false);
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.createCartItem(PRODUCT_ID, quantity);
        });
    }

    @Test
    void testCreateCartItem_DuplicateItem() {
        // Arrange
        int quantity = 5;
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                .thenReturn(Optional.of(testCartItem));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.createCartItem(PRODUCT_ID, quantity);
        });
    }

    @Test
    void testCreateCartItem_InsufficientQuantity() {
        // Arrange
        int quantity = 150; // Больше чем доступно (100)
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(testCart));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartItemQuantityException.class, () -> {
            cartItemService.createCartItem(PRODUCT_ID, quantity);
        });
        verify(productRepository, never()).save(any());
    }

    @Test
    void testUpdateCartItem_Success() {
        // Arrange
        int newQuantity = 10;
        testCart.getItems().add(testCartItem);
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(testCartItem));
        // Важно: используем PRODUCT_ID напрямую, так как testCartItem.getProductId() возвращает PRODUCT_ID
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        CartItem result = cartItemService.updateCartItem(CART_ITEM_ID, newQuantity);

        // Assert
        assertNotNull(result);
        assertEquals(newQuantity, result.getQuantity());
        // Примечание: в текущей реализации productRepository.save() не вызывается в updateCartItem
        // хотя количество товара обновляется, но не сохраняется
        verify(cartItemRepository).save(testCartItem);
        verify(cartRepository).save(testCart);
    }

    @Test
    void testUpdateCartItem_UnpaidOrderExists() {
        // Arrange
        int newQuantity = 10;
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.updateCartItem(CART_ITEM_ID, newQuantity);
        });
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void testUpdateCartItem_CartItemNotFound() {
        // Arrange
        int newQuantity = 10;
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartItemAbsenceException.class, () -> {
            cartItemService.updateCartItem(CART_ITEM_ID, newQuantity);
        });
    }

    @Test
    void testUpdateCartItem_InvalidQuantity_Zero() {
        // Arrange
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(testCartItem));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.updateCartItem(CART_ITEM_ID, 0);
        });
    }

    @Test
    void testUpdateCartItem_ProductNotFound() {
        // Arrange
        int newQuantity = 10;
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(testCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductAbsenceException.class, () -> {
            cartItemService.updateCartItem(CART_ITEM_ID, newQuantity);
        });
    }

    @Test
    void testUpdateCartItem_InsufficientQuantity() {
        // Arrange
        int newQuantity = 150; // Больше чем доступно
        testProduct.setQuantity(50);
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(testCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(CartItemQuantityException.class, () -> {
            cartItemService.updateCartItem(CART_ITEM_ID, newQuantity);
        });
    }

    @Test
    void testDeleteCartItemById_Success() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(testCartItem));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        // Act
        cartItemService.deleteCartItemById(CART_ITEM_ID);

        // Assert
        verify(productRepository).save(testProduct);
        verify(cartItemRepository).delete(testCartItem);
        verify(cartRepository).save(testCart);
    }

    @Test
    void testDeleteCartItemById_CartItemNotFound() {
        // Arrange
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartItemAbsenceException.class, () -> {
            cartItemService.deleteCartItemById(CART_ITEM_ID);
        });
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void testDeleteCartItemById_UnpaidOrderExists() {
        // Arrange
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(testCartItem));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.deleteCartItemById(CART_ITEM_ID);
        });
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void testDeleteCartItemById_DifferentUser() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(999L);
        Cart otherCart = new Cart();
        otherCart.setUser(otherUser);
        testCartItem.setCart(otherCart);

        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(testCartItem));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cartItemService.deleteCartItemById(CART_ITEM_ID);
        });
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void testDeleteCartItemById_ProductNotFound() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(userService.getUserIdFromContext()).thenReturn(USER_ID);
        when(cartItemRepository.findById(CART_ITEM_ID)).thenReturn(Optional.of(testCartItem));
        when(orderRepository.existsByUserIdAndStatus(USER_ID, OrderStatus.UNPAID)).thenReturn(false);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductAbsenceException.class, () -> {
            cartItemService.deleteCartItemById(CART_ITEM_ID);
        });
    }

    @Test
    void testClearCartAndUpdateProductQuantities_Success() {
        // Arrange
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(cartItems);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        cartItemService.clearCartAndUpdateProductQuantities(CART_ID);

        // Assert
        verify(productRepository, times(cartItems.size())).save(any(Product.class));
        verify(cartItemRepository).deleteAll(cartItems);
    }

    @Test
    void testClearCartAndUpdateProductQuantities_ProductNotFound() {
        // Arrange
        List<CartItem> cartItems = List.of(testCartItem);
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(cartItems);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductAbsenceException.class, () -> {
            cartItemService.clearCartAndUpdateProductQuantities(CART_ID);
        });
    }

    @Test
    void testClearCartAndUpdateProductQuantities_EmptyCart() {
        // Arrange
        List<CartItem> emptyCartItems = new ArrayList<>();
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(emptyCartItems);

        // Act
        cartItemService.clearCartAndUpdateProductQuantities(CART_ID);

        // Assert
        verify(productRepository, never()).save(any());
        verify(cartItemRepository).deleteAll(emptyCartItems);
    }
}

