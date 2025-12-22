package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.Cart;
import com.blpsteam.blpslab1.data.entities.core.CartItem;
import com.blpsteam.blpslab1.data.entities.product.Product;
import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.OrderStatus;
import com.blpsteam.blpslab1.exceptions.impl.CartItemQuantityException;
import com.blpsteam.blpslab1.exceptions.impl.CartAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.CartItemAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.ProductAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.UserAbsenceException;
import com.blpsteam.blpslab1.repositories.product.ProductRepository;
import com.blpsteam.blpslab1.repositories.core.CartItemRepository;
import com.blpsteam.blpslab1.repositories.core.CartRepository;
import com.blpsteam.blpslab1.repositories.core.OrderRepository;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.service.CartItemService;
import com.blpsteam.blpslab1.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public CartItem getCartItemById(Long id) {
        return cartItemRepository.findById(id)
                .orElseThrow(() -> new CartItemAbsenceException("Элемент корзины с id " + id + " не найден"));
    }

    @Override
    public Page<CartItem> getAllCartItems(Pageable pageable) {
        Long userId = userService.getUserIdFromContext();

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() ->
                new CartAbsenceException("У пользователя (id = " + userId + ") нет корзины"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        return new PageImpl<>(cartItems, pageable, cartItems.size());
    }

    @Override
    @Transactional
    public CartItem createCartItem(Long productId, int quantity) {
        log.info("CreateCartItem method called");
        if (quantity <= 0){
            throw new IllegalArgumentException("Измените количество товара, так как количество должно быть больше 0");
        }

        Cart cart = cartRepository.findByUserId(userService.getUserIdFromContext())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    User user = userRepository.findById(userService.getUserIdFromContext())
                            .orElseThrow(() -> new UserAbsenceException("Пользователь с id " + userService.getUserIdFromContext() + " не найден"));
                    newCart.setUser(user);
                    log.info("Creating new cart for user {}", user.getId());
                    return cartRepository.save(newCart);
                });
        log.info("Cart exists for user {}, so continue", cart.getUser().getId());
        Long userId = cart.getUser().getId();
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            throw new IllegalArgumentException("Нельзя редактировать корзину, пока у вас есть неоплаченный заказ");
        }

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductAbsenceException("Товар с таким id не существует")
        );
        if (!product.getApproved()){
            throw new IllegalArgumentException("Нет одобренного товара с id " + product.getId());
        }

        if (cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).isPresent()) {
            throw new IllegalArgumentException("Элемент корзины с этим товаром уже существует");
        }
        int newQuantity = product.getQuantity() - quantity;
        if (newQuantity >= 0) {
            product.setQuantity(newQuantity);
            productRepository.save(product);
            
            CartItem cartItem = new CartItem();
            cartItem.setQuantity(quantity);
            cartItem.setProductId(productId);
            cartItem.setUnitPrice(product.getPrice().intValue());
            cartItem.setTotalPrice(product.getPrice().intValue() * quantity);
            cartItem.setCart(cart);
            cartItem = cartItemRepository.save(cartItem);
            cart.addItem(cartItem);
            cartRepository.save(cart);
            log.info("Cart item with id {} created for user {}", cartItem.getId(), cart.getUser().getId());
            return cartItem;
        }
        throw new CartItemQuantityException("Недостаточно товара");
    }

    @Override
    @Transactional
    public CartItem updateCartItem(Long id, int quantity) {
        log.info("UpdateCartItem method called");
        Long userId = userService.getUserIdFromContext();
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            throw new IllegalArgumentException("Нельзя редактировать корзину, пока у вас есть неоплаченный заказ");
        }
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new CartItemAbsenceException("Элемент корзины не существует"));
        int previousQuantity = cartItem.getQuantity();

        if (quantity <= 0){
            throw new IllegalArgumentException("Измените количество товара, так как количество должно быть больше 0");
        }
        Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(
                () -> new ProductAbsenceException("Товар с таким id не существует")
        );
        int unitPrice = product.getPrice().intValue();
        int totalPrice = unitPrice * quantity;

        cartItem.setQuantity(quantity);
        int newQuantity = product.getQuantity() - quantity + previousQuantity;
        if (newQuantity >= 0) {
            Cart cart = cartItem.getCart();
            product.setQuantity(newQuantity);
            cartItem.setUnitPrice(unitPrice);
            cartItem.setTotalPrice(totalPrice);
            cartItemRepository.save(cartItem);

            cart.updateTotalPrice();
            cartRepository.save(cart);
            log.info("Cart item with id {} updated for user {}", cartItem.getId(), cart.getUser().getId());
            return cartItem;
        }
        throw new CartItemQuantityException("Недостаточно товара");
    }

    @Override
    @Transactional
    public void deleteCartItemById(Long id) {
        log.info("DeleteCartItemById method called");
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new CartItemAbsenceException("Элемент корзины не существует"));

        Long userId=userService.getUserIdFromContext();
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            throw new IllegalArgumentException("Нельзя редактировать корзину, пока у вас есть неоплаченный заказ");
        }

        Cart cart = cartItem.getCart();
        if (!cart.getUser().getId().equals(userService.getUserIdFromContext())){
            throw new IllegalArgumentException("Вы не можете удалить элемент не из своей корзины");
        }
        cart.removeItem(cartItem);


        Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(
                () -> new ProductAbsenceException("Товар с таким id не существует")
        );
        int quantity = product.getQuantity();
        product.setQuantity(quantity + cartItem.getQuantity());
        productRepository.save(product);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);
        log.info("Cart item with id {} deleted for user {}", id, userId);

    }

    @Override
    @Transactional
    public void clearCartAndUpdateProductQuantities(Long cartId) {
        log.info("ClearCartAndUpdateProductQuantities method called");
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(
                    () -> new ProductAbsenceException("Товар с таким id не существует")
            );
            int quantity = product.getQuantity();
            product.setQuantity(quantity + cartItem.getQuantity());
            productRepository.save(product);
        }
        cartItemRepository.deleteAll(cartItems);
    }

}
