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
        log.info("Попытка добавления товара в корзину. Товар ID: {}, количество: {}", productId, quantity);
        
        if (quantity <= 0){
            log.warn("Попытка добавления товара с некорректным количеством: {}. Товар ID: {}", quantity, productId);
            throw new IllegalArgumentException("Измените количество товара, так как количество должно быть больше 0");
        }

        Long userId = userService.getUserIdFromContext();
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserAbsenceException("Пользователь с id " + userId + " не найден"));
                    newCart.setUser(user);
                    log.info("Создана новая корзина для пользователя ID: {}", user.getId());
                    return cartRepository.save(newCart);
                });
        log.info("Корзина найдена для пользователя ID: {}", cart.getUser().getId());
        
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            log.warn("Попытка добавления товара в корзину при наличии неоплаченного заказа. Пользователь ID: {}", userId);
            throw new IllegalArgumentException("Нельзя редактировать корзину, пока у вас есть неоплаченный заказ");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Попытка добавления несуществующего товара ID: {} в корзину", productId);
                    return new ProductAbsenceException("Товар с таким id не существует");
                });
        
        if (!product.getApproved()){
            log.warn("Попытка добавления неодобренного товара ID: {} в корзину", product.getId());
            throw new IllegalArgumentException("Нет одобренного товара с id " + product.getId());
        }

        if (cartItemRepository.findByCartIdAndProductId(cart.getId(), productId).isPresent()) {
            log.warn("Попытка добавления уже существующего товара ID: {} в корзину ID: {}", productId, cart.getId());
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
            log.info("Товар успешно добавлен в корзину. Элемент корзины ID: {}, товар ID: {}, количество: {}, пользователь ID: {}", 
                    cartItem.getId(), productId, quantity, cart.getUser().getId());
            return cartItem;
        }
        log.warn("Недостаточно товара для добавления в корзину. Товар ID: {}, запрошено: {}, доступно: {}", 
                productId, quantity, product.getQuantity());
        throw new CartItemQuantityException("Недостаточно товара");
    }

    @Override
    @Transactional
    public CartItem updateCartItem(Long id, int quantity) {
        log.info("Попытка обновления элемента корзины. Элемент ID: {}, новое количество: {}", id, quantity);
        Long userId = userService.getUserIdFromContext();
        
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            log.warn("Попытка обновления элемента корзины при наличии неоплаченного заказа. Пользователь ID: {}", userId);
            throw new IllegalArgumentException("Нельзя редактировать корзину, пока у вас есть неоплаченный заказ");
        }
        
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка обновления несуществующего элемента корзины ID: {}", id);
                    return new CartItemAbsenceException("Элемент корзины не существует");
                });
        int previousQuantity = cartItem.getQuantity();

        if (quantity <= 0){
            log.warn("Попытка обновления элемента корзины ID: {} с некорректным количеством: {}", id, quantity);
            throw new IllegalArgumentException("Измените количество товара, так как количество должно быть больше 0");
        }
        
        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> {
                    log.warn("Товар не найден при обновлении элемента корзины. Товар ID: {}, элемент корзины ID: {}", 
                            cartItem.getProductId(), id);
                    return new ProductAbsenceException("Товар с таким id не существует");
                });
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
            log.info("Элемент корзины успешно обновлен. Элемент ID: {}, количество: {} -> {}, пользователь ID: {}", 
                    cartItem.getId(), previousQuantity, quantity, cart.getUser().getId());
            return cartItem;
        }
        log.warn("Недостаточно товара для обновления элемента корзины. Элемент ID: {}, товар ID: {}, запрошено: {}, доступно: {}", 
                id, cartItem.getProductId(), quantity, product.getQuantity() + previousQuantity);
        throw new CartItemQuantityException("Недостаточно товара");
    }

    @Override
    @Transactional
    public void deleteCartItemById(Long id) {
        log.info("Попытка удаления элемента корзины ID: {}", id);
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка удаления несуществующего элемента корзины ID: {}", id);
                    return new CartItemAbsenceException("Элемент корзины не существует");
                });

        Long userId = userService.getUserIdFromContext();
        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.UNPAID)){
            log.warn("Попытка удаления элемента корзины при наличии неоплаченного заказа. Пользователь ID: {}", userId);
            throw new IllegalArgumentException("Нельзя редактировать корзину, пока у вас есть неоплаченный заказ");
        }

        Cart cart = cartItem.getCart();
        if (!cart.getUser().getId().equals(userId)){
            log.warn("Попытка удаления элемента корзины другого пользователя. Элемент ID: {}, владелец корзины ID: {}, запросивший ID: {}", 
                    id, cart.getUser().getId(), userId);
            throw new IllegalArgumentException("Вы не можете удалить элемент не из своей корзины");
        }
        
        cart.removeItem(cartItem);
        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> {
                    log.warn("Товар не найден при удалении элемента корзины. Товар ID: {}, элемент корзины ID: {}", 
                            cartItem.getProductId(), id);
                    return new ProductAbsenceException("Товар с таким id не существует");
                });
        
        int quantity = product.getQuantity();
        product.setQuantity(quantity + cartItem.getQuantity());
        productRepository.save(product);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);
        log.info("Элемент корзины успешно удален. Элемент ID: {}, товар ID: {}, возвращено количество: {}, пользователь ID: {}", 
                id, cartItem.getProductId(), cartItem.getQuantity(), userId);
    }

    @Override
    @Transactional
    public void clearCartAndUpdateProductQuantities(Long cartId) {
        log.info("Очистка корзины и обновление количества товаров. Корзина ID: {}", cartId);
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        log.info("Найдено элементов в корзине для очистки: {}", cartItems.size());

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> {
                        log.warn("Товар не найден при очистке корзины. Товар ID: {}, элемент корзины ID: {}", 
                                cartItem.getProductId(), cartItem.getId());
                        return new ProductAbsenceException("Товар с таким id не существует");
                    });
            int quantity = product.getQuantity();
            product.setQuantity(quantity + cartItem.getQuantity());
            productRepository.save(product);
            log.debug("Количество товара ID: {} восстановлено: {} -> {}", 
                    cartItem.getProductId(), quantity, product.getQuantity());
        }
        cartItemRepository.deleteAll(cartItems);
        log.info("Корзина ID: {} успешно очищена, количество товаров восстановлено для {} элементов", 
                cartId, cartItems.size());
    }

}
