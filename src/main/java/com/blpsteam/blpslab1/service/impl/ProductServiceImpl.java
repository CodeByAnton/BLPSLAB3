package com.blpsteam.blpslab1.service.impl;

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
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductAbsenceException("Товар с данным id не существует"));
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> getApprovedProducts(String name, Pageable pageable) {
        Page<Product> products;

        if (name != null && !name.isEmpty()) {
            products = productRepository.findByApprovedAndNameContainingIgnoreCase(true, name, pageable);
            if (products.isEmpty()) {
                throw new ProductAbsenceException("Товар с таким именем не найден. Пожалуйста, измените вводимое имя.");
            }
        } else {
            products = productRepository.findByApproved(true, pageable);
        }

        return products;
    }

    @Override
    @Transactional
    public Product addProduct(String brand, String name, String description, int quantity, Long price, Double averageRating, int reviewsCount) {
        log.info("AddProduct method");
        if (name==null || name.isEmpty()) {
            throw new IllegalArgumentException("Название не может быть null или пустым");
        }
        if (description==null || description.isEmpty()) {
            throw new IllegalArgumentException("Описание не может быть null или пустым");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Количество не может быть отрицательным");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }

        Long userId = userService.getUserIdFromContext();
        User seller = userRepository.findById(userId).orElseThrow(() -> new UserAbsenceException("Пользователь не найден"));

        Optional<Product> existingProduct = productRepository.findByBrandAndNameAndDescriptionAndSellerId(brand, name, description, seller.getId());

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setQuantity(product.getQuantity() + quantity);
            log.info("Product added successfully");
            return productRepository.save(product);
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
            log.info("New Product added successfully, but not approved");
            return productRepository.save(product);
        }
    }

    @Override
    @Transactional
    public boolean approveProduct(Long productId) {
        log.info("Approving product with id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductAbsenceException("Товар с таким id не найден. Пожалуйста, измените вводимый id."));
        product.setApproved(true);
        productRepository.save(product);
        log.info("Product {} approved successfully", productId);
        return true;
    }
}