package com.blpsteam.blpslab1.configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Getter
public class EnvironmentProperties {
    private final String yooKassaShopId;
    private final String yooKassaApiKey;
    private final String yooKassaApiUrl;
    private final String yooKassaReturnUrl;

    public EnvironmentProperties(
            @Value("${YOOKASSA_SHOP_ID:}") String shopId,
            @Value("${YOOKASSA_API_KEY:}") String apiKey,
            @Value("${YOOKASSA_API_URL:}") String apiUrl,
            @Value("${YOOKASSA_RETURN_URL:}") String returnUrl) {
        this.yooKassaShopId = loadFromEnvOrFile("YOOKASSA_SHOP_ID", shopId);
        this.yooKassaApiKey = loadFromEnvOrFile("YOOKASSA_API_KEY", apiKey);
        this.yooKassaApiUrl = loadFromEnvOrFile("YOOKASSA_API_URL", apiUrl);
        this.yooKassaReturnUrl = loadFromEnvOrFile("YOOKASSA_RETURN_URL", returnUrl);
    }

    private String loadFromEnvOrFile(String key, String defaultValue) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        if (defaultValue != null && !defaultValue.isEmpty()) {
            return defaultValue;
        }

        Map<String, String> envVars = loadEnvFile();
        String fileValue = envVars.get(key);
        if (fileValue != null && !fileValue.isEmpty()) {
            return fileValue;
        }

        return defaultValue;
    }

    private Map<String, String> loadEnvFile() {
        Map<String, String> envVars = new HashMap<>();
        try {
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                log.info("Loading YooKassa credentials from .env file");
                Files.lines(envPath)
                        .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                        .filter(line -> line.contains("="))
                        .forEach(line -> {
                            String[] parts = line.split("=", 2);
                            if (parts.length == 2) {
                                String key = parts[0].trim();
                                String value = parts[1].trim();
                                if (key.startsWith("YOOKASSA_")) {
                                    envVars.put(key, value);
                                }
                            }
                        });
                log.info("Loaded {} YooKassa variables from .env file", envVars.size());
            } else {
                log.warn(".env file not found in project root");
            }
        } catch (IOException e) {
            log.warn("Failed to read .env file: {}", e.getMessage());
        }
        return envVars;
    }

    

    public void validateYooKassaConfig() {
        if (yooKassaShopId == null || yooKassaShopId.isEmpty()) {
            throw new IllegalStateException("YOOKASSA_SHOP_ID не установлен. " +
                    "Пожалуйста, установите его как переменную окружения или в файле .env.");
        }
        if (yooKassaApiKey == null || yooKassaApiKey.isEmpty()) {
            throw new IllegalStateException("YOOKASSA_API_KEY не установлен. " +
                    "Пожалуйста, установите его как переменную окружения или в файле .env.");
        }
        log.info("YooKassa configuration loaded successfully. Shop ID: {}", yooKassaShopId);
    }
}

