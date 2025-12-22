package com.blpsteam.blpslab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Blpslab1Application {

    public static void main(String[] args) {
        SpringApplication.run(Blpslab1Application.class, args);
    }
}
