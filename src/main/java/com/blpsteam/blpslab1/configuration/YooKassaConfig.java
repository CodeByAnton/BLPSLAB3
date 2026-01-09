package com.blpsteam.blpslab1.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class YooKassaConfig {
    private final EnvironmentProperties envProps;
    private String apiUrl;
    private String returnUrl;

    public YooKassaConfig(EnvironmentProperties envProps) {
        this.envProps = envProps;
    }

    @PostConstruct
    public void init() {
        envProps.validateYooKassaConfig();
        this.apiUrl = envProps.getYooKassaApiUrl();
        this.returnUrl = envProps.getYooKassaReturnUrl();
    }

    public String getShopId() {
        return envProps.getYooKassaShopId();
    }

    public String getApiKey() {
        return envProps.getYooKassaApiKey();
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }
}

