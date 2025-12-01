package com.blpsteam.blpslab1.security;

import com.blpsteam.blpslab1.data.entities.core.User;
import com.blpsteam.blpslab1.exceptions.InvalidCredentialsException;
import com.blpsteam.blpslab1.exceptions.UsernameNotFoundException;
import com.blpsteam.blpslab1.repositories.core.UserRepository;
import com.blpsteam.blpslab1.util.SpringContext;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;

public class JaasLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private UserRepository userRepository;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.userRepository = SpringContext.getBean(UserRepository.class);
    }

    @Override
    public boolean login() throws LoginException {
        try {
            NameCallback nameCallback = new NameCallback("username");
            PasswordCallback passwordCallback = new PasswordCallback("password", false);

            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});

            String username = nameCallback.getName();
            String password = new String(passwordCallback.getPassword());

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder =
                    SpringContext.getBean(org.springframework.security.crypto.password.PasswordEncoder.class);

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new InvalidCredentialsException("Invalid credentials");
            }

            subject.getPrincipals().add(new UserPrincipal(user.getUsername()));
            return true;

        } catch (IOException | UnsupportedCallbackException e) {
            throw new LoginException("Callback error: " + e.getMessage());
        }
    }

    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        return true;
    }
}
