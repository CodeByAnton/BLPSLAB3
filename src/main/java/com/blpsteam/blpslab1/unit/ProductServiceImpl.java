package com.blpsteam.blpslab1.unit;

import com.blpsteam.blpslab1.data.entities.product.Product;
import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.exceptions.impl.ProductAbsenceException;
import com.blpsteam.blpslab1.exceptions.impl.UserAbsenceException;
import com.blpsteam.blpslab1.repositories.product.ProductRepository;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.service.ProductService;
import com.blpsteam.blpslab1.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;


    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> getApprovedProducts(String name, Pageable pageable) {
        log.info("Получение списка одобренных товаров. Поиск: {}", name);
        Page<Product> products;

        if (name != null && !name.isEmpty()) {
            products = productRepository.findByApprovedAndNameContainingIgnoreCase(true, name, pageable);
            if (products.isEmpty()) {
                log.warn("Товар с именем '{}' не найден", name);
                throw new ProductAbsenceException("Товар с таким именем не найден. Пожалуйста, измените вводимое имя.");
            }
            log.info("Найдено товаров по запросу '{}': {}", name, products.getTotalElements());
        } else {
            products = productRepository.findByApproved(true, pageable);
            log.info("Получено всех одобренных товаров: {}", products.getTotalElements());
        }

        return products;
    }

    @Override
    @Transactional
    public Product addProduct(String brand, String name, String description, int quantity, Long price, Double averageRating, int reviewsCount) {
        log.info("Попытка добавления товара: {} (бренд: {})", name, brand);
        if (name==null || name.isEmpty()) {
            log.warn("Попытка добавления товара с пустым названием");
            throw new IllegalArgumentException("Название не может быть null или пустым");
        }
        if (description==null || description.isEmpty()) {
            log.warn("Попытка добавления товара '{}' с пустым описанием", name);
            throw new IllegalArgumentException("Описание не может быть null или пустым");
        }
        if (quantity < 0) {
            log.warn("Попытка добавления товара '{}' с отрицательным количеством: {}", name, quantity);
            throw new IllegalArgumentException("Количество не может быть отрицательным");
        }
        if (price < 0) {
            log.warn("Попытка добавления товара '{}' с отрицательной ценой: {}", name, price);
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }

        Long userId = userService.getUserIdFromContext();
        User seller = userRepository.findById(userId).orElseThrow(() -> new UserAbsenceException("Пользователь не найден"));

        Optional<Product> existingProduct = productRepository.findByBrandAndNameAndDescriptionAndSellerId(brand, name, description, seller.getId());

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            int oldQuantity = product.getQuantity();
            product.setQuantity(product.getQuantity() + quantity);
            Product saved = productRepository.save(product);
            log.info("Количество товара '{}' (ID: {}) обновлено: {} -> {}", name, saved.getId(), oldQuantity, saved.getQuantity());
            return saved;
        } else {
            Product product = new Product();
            product.setBrand(brand);
            product.setName(name);
            product.setDescription(description);
            product.setQuantity(quantity);
            product.setPrice(price);
            product.setApproved(false);
            product.setSellerId(seller.getId());
            product.setAverageRating(0d);
            product.setReviewCount(0);
            Product saved = productRepository.save(product);
            log.info("Новый товар '{}' (ID: {}) успешно добавлен продавцом (ID: {}), ожидает одобрения", name, saved.getId(), seller.getId());
            return saved;
        }
    }

    @Override
    @Transactional
    public boolean approveProduct(Long productId) {
        log.info("Попытка одобрения товара с ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Попытка одобрения несуществующего товара с ID: {}", productId);
                    return new ProductAbsenceException("Товар с таким id не найден. Пожалуйста, измените вводимый id.");
                });
        product.setApproved(true);
        productRepository.save(product);
        log.info("Товар '{}' (ID: {}) успешно одобрен администратором", product.getName(), productId);
        return true;
    }
}