package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.Cart;
import com.blpsteam.blpslab1.data.entities.core.CartItem;
import com.blpsteam.blpslab1.data.entities.product.Product;
import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.OrderStatus;
import com.blpsteam.blpslab1.dto.CartItemQuantityRequestDTO;
import com.blpsteam.blpslab1.dto.CartItemRequestDTO;
import com.blpsteam.blpslab1.dto.CartItemResponseDTO;
import com.blpsteam.blpslab1.exceptions.CartItemQuantityException;
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
import jakarta.persistence.PersistenceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartItemServiceImpl implements CartItemService {

    private static final Logger log = LoggerFactory.getLogger(CartItemServiceImpl.class);
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public CartItemServiceImpl(CartItemRepository cartItemRepository, ProductRepository productRepository, CartRepository cartRepository, UserService userService, UserRepository userRepository, OrderRepository orderRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public CartItemResponseDTO getCartItemById(Long id) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new CartItemAbsenceException("CartItem with id " + id + " not found"));
        return getCartItemResponseDTOFromEntity(cartItem);
    }

    @Override
    public Page<CartItemResponseDTO> getAllCartItems(Pageable pageable) {
        Long userId = userService.getUserIdFromContext();

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() ->
                new CartAbsenceException("Users (id = " + userId + ") doesn't have a cart"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        return new PageImpl<>(cartItems.stream()
                .map(this::getCartItemResponseDTOFromEntity)
                .collect(Collectors.toList()), pageable, cartItems.size());
    }

    @Override
    @Transactional
    public CartItemResponseDTO createCartItem(CartItemRequestDTO cartItemRequestDTO) {
        log.info("CreateCartItem method called");
        if (cartItemRequestDTO.quantity()<=0){
            throw new IllegalArgumentException("Change product quantity, because quantity should be greater than 0");
        }

        Cart cart = cartRepository.findByUserId(userService.getUserIdFromContext())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    User user = userRepository.findById(userService.getUserIdFromContext())
                            .orElseThrow(() -> new UserAbsenceException("There is no user with id " + userService.getUserIdFromContext()));
                    newCart.setUser(user);
                    System.out.println("Create cart because not exist");
                    return cartRepository.save(newCart);
                });
        log.info("Cart exists for user {}, so continue", cart.getUser().getId());
        Long userId=cart.getUser().getId();
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            throw new IllegalArgumentException("You can't edit cart while you have unpaid order");
        }

        CartItem cartItem = getCartItemFromDTO(cartItemRequestDTO, userId);
        Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(
                () -> new ProductAbsenceException("There is no produt with this id")
        );
        if (!product.getApproved()){
            throw new IllegalArgumentException("There is no approved product with id " + product.getId());
        }

        if (cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()).isPresent()) {
            throw new IllegalArgumentException("CartItem with this product already exists");
        }
        int newQuantity = product.getQuantity() - cartItem.getQuantity();
        if (newQuantity >= 0) {
            product.setQuantity(newQuantity);
            productRepository.save(product);

//            if (true) {
//                throw new PersistenceException("testsing transcation");
//            }
            cartItem = cartItemRepository.save(cartItem);
            cart.addItem(cartItem);
            cartRepository.save(cart);
            log.info("Cart item with id {} created for user {}", cartItem.getId(),cart.getUser().getId());
            return getCartItemResponseDTOFromEntity(cartItem);
        }
        throw new CartItemQuantityException("Insufficient product");
    }

    @Override
    @Transactional
    public CartItemResponseDTO updateCartItem(Long id, CartItemQuantityRequestDTO cartItemRequestDTO) {
        log.info("UpdateCartItem method called");
        Long userId=userService.getUserIdFromContext();
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            throw new IllegalArgumentException("You can't edit cart while you have unpaid order");
        }
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new CartItemAbsenceException("CartItem doesn't exist"));
        int previousQuantity = cartItem.getQuantity();

        if (cartItemRequestDTO.quantity()<=0){
            throw new IllegalArgumentException("Change product quantity, because quantity should be greater than 0");
        }
        Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(
                () -> new ProductAbsenceException("There is no produt with this id")
        );
        int unitPrice = product.getPrice().intValue();
        int totalPrice = unitPrice * cartItemRequestDTO.quantity();

        cartItem.setQuantity(cartItemRequestDTO.quantity());
        int newQuantity = product.getQuantity() - cartItem.getQuantity()+previousQuantity;
        if (newQuantity >= 0) {
            Cart cart=cartItem.getCart();
            product.setQuantity(newQuantity);
            cartItem.setUnitPrice(unitPrice);
            cartItem.setTotalPrice(totalPrice);
            cartItemRepository.save(cartItem);

            cart.updateTotalPrice();
            cartRepository.save(cart);
            log.info("Cart item with id {} updated for user {}", cartItem.getId(),cart.getUser().getId());
            return getCartItemResponseDTOFromEntity(cartItem);
        }
        throw new CartItemQuantityException("Not enough product quantity");
    }

    @Override
    @Transactional
    public void deleteCartItemById(Long id) {
        log.info("DeleteCartItemById method called");
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new CartItemAbsenceException("CartItem doesn't exist"));

        Long userId=userService.getUserIdFromContext();
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            throw new IllegalArgumentException("You can't edit cart while you have unpaid order");
        }

        Cart cart = cartItem.getCart();
        if (!cart.getUser().getId().equals(userService.getUserIdFromContext())){
            throw new IllegalArgumentException("You can't remove item not from your cart");
        }
        cart.removeItem(cartItem);


        Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(
                () -> new ProductAbsenceException("There is no produt with this id")
        );
        int quantity = product.getQuantity();
        product.setQuantity(quantity + cartItem.getQuantity()); // Возвращаем товар в магазин
        productRepository.save(product); // Сохраняем изменения в продукте

        cartItemRepository.delete(cartItem); // Удаляем cartItem из репозитория
        cartRepository.save(cart);
        log.info("Cart item with id {} deleted for user {}", id, userId);

    }

    @Override
    @Transactional
    public void clearCartAndUpdateProductQuantities(Long cartId) {
        log.info("ClearCartAndUpdateProductQuantities method called");
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);

        System.out.println(cartItems);
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(
                    () -> new ProductAbsenceException("There is no produt with this id")
            );
            int quantity = product.getQuantity();
            product.setQuantity(quantity + cartItem.getQuantity());
            productRepository.save(product);
        }
        cartItemRepository.deleteAll(cartItems);
    }

    private CartItem getCartItemFromDTO(CartItemRequestDTO cartItemRequestDTO, Long userId) {
        CartItem cartItem = new CartItem();
        cartItem.setQuantity(cartItemRequestDTO.quantity());
        Product product = productRepository.findById(cartItemRequestDTO.productId())
                .orElseThrow(() -> new ProductAbsenceException("Product с данным id не существует"));
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(()-> new CartAbsenceException("Cart с данным id не существует"));
        cartItem.setProductId(cartItemRequestDTO.productId());
        cartItem.setUnitPrice(product.getPrice().intValue());
        cartItem.setTotalPrice(product.getPrice().intValue()*cartItemRequestDTO.quantity());
        cartItem.setCart(cart);
        return cartItem;
    }


    private CartItemResponseDTO getCartItemResponseDTOFromEntity(CartItem cartItem) {
        return new CartItemResponseDTO(
                cartItem.getId(),
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                cartItem.getTotalPrice(),
                cartItem.getCart().getId(),
                cartItem.getProductId()
        );
    }
}
