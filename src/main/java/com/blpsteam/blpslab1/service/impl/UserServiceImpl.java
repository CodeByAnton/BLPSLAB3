package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.Role;
import com.blpsteam.blpslab1.exceptions.impl.AdminAlreadyExistsException;
import com.blpsteam.blpslab1.exceptions.impl.InvalidCredentialsException;
import com.blpsteam.blpslab1.exceptions.impl.UsernameAlreadyExistsException;
import com.blpsteam.blpslab1.exceptions.impl.UsernameNotFoundException;
import com.blpsteam.blpslab1.exceptions.impl.UserAbsenceException;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.security.JaasCallbackHandler;
import com.blpsteam.blpslab1.service.JwtService;
import com.blpsteam.blpslab1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;

    @Override
    public User register(String username, String password, Role role) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }


        if (role == Role.ADMIN && userRepository.existsByRole(Role.ADMIN)) {
            throw new AdminAlreadyExistsException("Администратор уже существует");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException("Имя пользователя уже занято");
        }


        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setBalance(100000L);
        return userRepository.save(user);
    }

    @Override
    public String login(String username, String password) {
        try {
            LoginContext loginContext = new LoginContext("MyLoginModule",
                    new JaasCallbackHandler(username, password));
            loginContext.login();

            return userRepository.findByUsername(username)
                    .map(jwtService::generateToken)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: "+username));

        } catch (LoginException e) {
            throw new InvalidCredentialsException("Неверное имя пользователя или пароль");
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        return findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    @Override
    public Long getUserIdFromContext() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByUsername(userDetails.getUsername())
                    .map(User::getId)
                    .orElseThrow(() -> new UserAbsenceException("Такого пользователя не существует"));
        }

        throw new UserAbsenceException("Такого пользователя не существует");
    }

    @Override
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

