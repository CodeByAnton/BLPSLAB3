package com.blpsteam.blpslab1.unit;

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
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.register(USERNAME, PASSWORD, Role.BUYER);

        assertNotNull(result);
        assertEquals(USERNAME, result.getUsername());
        assertEquals(Role.BUYER, result.getRole());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));

        assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.register(USERNAME, PASSWORD, Role.BUYER);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_AdminAlreadyExists() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        assertThrows(AdminAlreadyExistsException.class, () -> {
            userService.register(USERNAME, PASSWORD, Role.ADMIN);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmptyUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register("", PASSWORD, Role.BUYER);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_EmptyPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(USERNAME, "", Role.BUYER);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

        String token = userService.login(USERNAME, PASSWORD);

        assertNotNull(token);
        assertEquals("jwt-token", token);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.login(USERNAME, PASSWORD);
        });
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void testLogin_InvalidPassword() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            userService.login(USERNAME, PASSWORD);
        });
        verify(jwtService, never()).generateToken(any());
    }
}

