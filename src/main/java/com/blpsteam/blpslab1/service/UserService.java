package com.blpsteam.blpslab1.service;


import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.Role;
import com.blpsteam.blpslab1.exceptions.AdminAlreadyExistsException;
import com.blpsteam.blpslab1.exceptions.InvalidCredentialsException;
import com.blpsteam.blpslab1.exceptions.UsernameAlreadyExistsException;
import com.blpsteam.blpslab1.exceptions.UsernameNotFoundException;
import com.blpsteam.blpslab1.exceptions.impl.UserAbsenceException;
import com.blpsteam.blpslab1.repositories.core.UserRepository;

import com.blpsteam.blpslab1.security.JaasCallbackHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

    }

    public User register(String username, String password, Role role) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }


        if (role == Role.ADMIN && userRepository.existsByRole(Role.ADMIN)) {
            throw new AdminAlreadyExistsException("Admin user already exists");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }


        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setBalance(100000L);
        return userRepository.save(user);
    }

    public String login(String username, String password) {
        try {
            // Аутентификация через JAAS
            LoginContext loginContext = new LoginContext("MyLoginModule",
                    new JaasCallbackHandler(username, password));
            loginContext.login();

            return userRepository.findByUsername(username)
                    .map(jwtService::generateToken)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: "+username));

        } catch (LoginException e) {
            throw new InvalidCredentialsException("Wrong username or password");
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public boolean checkCredentials(String username, String password) {
        return findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    public Long getUserIdFromContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByUsername(userDetails.getUsername())
                    .map(User::getId)
                    .orElseThrow(() -> new UserAbsenceException("Такого пользователя не существует"));
        }

        throw new UserAbsenceException("Такого пользователя не существует");
    }

    public Role getUserRoleFromContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByUsername(userDetails.getUsername())
                    .map(User::getRole)
                    .orElseThrow(() -> new UserAbsenceException("Такого пользователя не существует"));
        }

        throw new UserAbsenceException("Такого пользователя не существует");
    }
}
