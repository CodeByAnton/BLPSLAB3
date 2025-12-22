package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.Role;
import com.blpsteam.blpslab1.exceptions.impl.AdminAlreadyExistsException;
import com.blpsteam.blpslab1.exceptions.impl.InvalidCredentialsException;
import com.blpsteam.blpslab1.exceptions.impl.UsernameAlreadyExistsException;
import com.blpsteam.blpslab1.exceptions.impl.UsernameNotFoundException;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername(USERNAME);
        testUser.setPassword(ENCODED_PASSWORD);
        testUser.setRole(Role.BUYER);
        testUser.setBalance(100000L);
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.register(USERNAME, PASSWORD, Role.BUYER);

        // Assert
        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(Role.BUYER, result.getRole());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.register(USERNAME, PASSWORD, Role.BUYER);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_AdminAlreadyExists() {
        // Arrange
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        // Act & Assert
        assertThrows(AdminAlreadyExistsException.class, () -> {
            userService.register(USERNAME, PASSWORD, Role.ADMIN);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmptyUsername() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register("", PASSWORD, Role.BUYER);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmptyPassword() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(USERNAME, "", Role.BUYER);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

        // Act
        String token = userService.login(USERNAME, PASSWORD);

        // Assert
        assertNotNull(token);
        assertEquals("jwt-token", token);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.login(USERNAME, PASSWORD);
        });
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(USERNAME, PASSWORD);
        });
        verify(jwtService, never()).generateToken(any());
    }
}

