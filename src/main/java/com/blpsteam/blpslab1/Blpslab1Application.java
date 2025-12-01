package com.blpsteam.blpslab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Blpslab1Application {

    public static void main(String[] args) {
        System.setProperty("java.security.auth.login.config",
                Blpslab1Application.class.getClassLoader().getResource("jaas.config").toString());
        SpringApplication.run(Blpslab1Application.class, args);
    }
}
