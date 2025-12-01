package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.exceptions.UsernameNotFoundException;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.service.BuyerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuyerServiceImpl implements BuyerService {

    private final UserRepository userRepository;
    public BuyerServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void increaseBalance(String username, Long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setBalance(user.getBalance()+amount);
        userRepository.save(user);
    }
}
