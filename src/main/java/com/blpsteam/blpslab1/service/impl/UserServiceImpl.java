package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.data.enums.Role;
import com.blpsteam.blpslab1.exceptions.impl.AdminAlreadyExistsException;
import com.blpsteam.blpslab1.exceptions.impl.InvalidCredentialsException;
import com.blpsteam.blpslab1.exceptions.impl.UsernameAlreadyExistsException;
import com.blpsteam.blpslab1.exceptions.impl.UsernameNotFoundException;
import com.blpsteam.blpslab1.exceptions.impl.UserAbsenceException;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.service.JwtService;
import com.blpsteam.blpslab1.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
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
        log.info("Попытка регистрации пользователя: {}", username);

        if (username == null || username.isBlank()) {
            log.warn("Попытка регистрации с пустым именем пользователя");
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (password == null || password.isBlank()) {
            log.warn("Попытка регистрации с пустым паролем для пользователя: {}", username);
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        if (role == Role.ADMIN && userRepository.existsByRole(Role.ADMIN)) {
            log.warn("Попытка создания второго администратора: {}", username);
            throw new AdminAlreadyExistsException("Администратор уже существует");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("Попытка регистрации с уже существующим именем пользователя: {}", username);
            throw new UsernameAlreadyExistsException("Имя пользователя уже занято");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setBalance(100000L);
        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно зарегистрирован: {} с ролью {}", username, role);
        return savedUser;
    }

    @Override
    public String login(String username, String password) {
        log.info("Попытка входа пользователя: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Попытка входа несуществующего пользователя: {}", username);
                    return new UsernameNotFoundException("Пользователь не найден: " + username);
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Неверный пароль для пользователя: {}", username);
            throw new InvalidCredentialsException("Неверное имя пользователя или пароль");
        }

        String token = jwtService.generateToken(user);
        log.info("Пользователь успешно вошел в систему: {} с ролью {}", username, user.getRole());
        return token;
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

