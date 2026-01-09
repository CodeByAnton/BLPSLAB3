package com.blpsteam.blpslab1.unit;

import com.blpsteam.blpslab1.data.entities.product.Product;
import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.exceptions.impl.ProductAbsenceException;
import com.blpsteam.blpslab1.repositories.product.ProductRepository;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private User testUser;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("seller");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setBrand("TestBrand");
        testProduct.setName("TestProduct");
        testProduct.setDescription("Test Description");
        testProduct.setQuantity(100);
        testProduct.setPrice(1000L);
        testProduct.setApproved(true);
        testProduct.setSellerId(1L);
    }

    @Test
    void testGetAllProducts_Success() {
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<Product> result = productService.getAllProducts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void testGetApprovedProducts_WithName_Success() {
        String searchName = "Test";
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findByApprovedAndNameContainingIgnoreCase(eq(true), eq(searchName), any(Pageable.class)))
                .thenReturn(productPage);

        Page<Product> result = productService.getApprovedProducts(searchName, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByApprovedAndNameContainingIgnoreCase(true, searchName, pageable);
    }

    @Test
    void testGetApprovedProducts_WithName_NotFound() {
        String searchName = "NonExistent";
        Page<Product> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
        when(productRepository.findByApprovedAndNameContainingIgnoreCase(eq(true), eq(searchName), any(Pageable.class)))
                .thenReturn(emptyPage);

        assertThrows(ProductAbsenceException.class, () -> {
            productService.getApprovedProducts(searchName, pageable);
        });
    }

    @Test
    void testGetApprovedProducts_WithoutName_Success() {
        List<Product> products = List.of(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findByApproved(eq(true), any(Pageable.class))).thenReturn(productPage);

        Page<Product> result = productService.getApprovedProducts(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByApproved(true, pageable);
    }

    @Test
    void testAddProduct_NewProduct_Success() {
        when(userService.getUserIdFromContext()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findByBrandAndNameAndDescriptionAndSellerId(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.addProduct("TestBrand", "TestProduct", "Test Description", 100, 1000L, 0.0, 0);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testAddProduct_ExistingProduct_UpdateQuantity() {
        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setQuantity(50);
        
        when(userService.getUserIdFromContext()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(productRepository.findByBrandAndNameAndDescriptionAndSellerId(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        Product result = productService.addProduct("TestBrand", "TestProduct", "Test Description", 50, 1000L, 0.0, 0);

        assertNotNull(result);
        assertEquals(100, existingProduct.getQuantity());
        verify(productRepository).save(existingProduct);
    }

    @Test
    void testAddProduct_EmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            productService.addProduct("Brand", "", "Description", 100, 1000L, 0.0, 0);
        });
    }

    @Test
    void testAddProduct_NegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> {
            productService.addProduct("Brand", "Product", "Description", -1, 1000L, 0.0, 0);
        });
    }

    @Test
    void testApproveProduct_Success() {
        testProduct.setApproved(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        boolean result = productService.approveProduct(1L);

        assertTrue(result);
        assertTrue(testProduct.getApproved());
        verify(productRepository).save(testProduct);
    }

    @Test
    void testApproveProduct_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductAbsenceException.class, () -> {
            productService.approveProduct(1L);
        });
    }
}

