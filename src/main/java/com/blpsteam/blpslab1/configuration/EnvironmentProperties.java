package com.blpsteam.blpslab1.configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@Getter
public class EnvironmentProperties {
    
    @Value("${YOOKASSA_SHOP_ID:}")
    private String yooKassaShopId;
    
    @Value("${YOOKASSA_API_KEY:}")
    private String yooKassaApiKey;
    
    @Value("${YOOKASSA_API_URL:https://api.yookassa.ru/v3/payments}")
    private String yooKassaApiUrl;
    
    @Value("${YOOKASSA_RETURN_URL:https://se.ifmo.ru/}")
    private String yooKassaReturnUrl;

    @PostConstruct
    public void validateYooKassaConfig() {
        if (yooKassaShopId == null || yooKassaShopId.isEmpty()) {
            throw new IllegalStateException("YOOKASSA_SHOP_ID не установлен. " +
                    "Пожалуйста, установите его как переменную окружения в .env файле.");
        }
        if (yooKassaApiKey == null || yooKassaApiKey.isEmpty()) {
            throw new IllegalStateException("YOOKASSA_API_KEY не установлен. " +
                    "Пожалуйста, установите его как переменную окружения в .env файле.");
        }
        log.info("YooKassa configuration loaded successfully. Shop ID: {}", yooKassaShopId);
    }
}

