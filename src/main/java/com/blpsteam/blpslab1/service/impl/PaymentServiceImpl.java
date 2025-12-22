package com.blpsteam.blpslab1.service.impl;

import com.blpsteam.blpslab1.configuration.YooKassaConfig;
import com.blpsteam.blpslab1.exceptions.impl.PaymentException;
import com.blpsteam.blpslab1.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private YooKassaConfig yooKassaConfig;


    @Override
    public String createPayment(Long amount, Long orderId) {
        try {
            String json = """
                {
                  "amount": {
                    "value": "%s",
                    "currency": "RUB"
                  },
                  "confirmation": {
                    "type": "redirect",
                    "return_url": "%s"
                  },
                  "metadata": {
                    "order_id":"%s"
                  },
                  "capture": true,
                  "description": "%s"
                }
                """.formatted(amount.toString(), yooKassaConfig.getReturnUrl(), orderId.toString(), "Order payment");

            log.info("Creating payment for order {} with amount {}", orderId, amount);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(yooKassaConfig.getApiUrl()))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Idempotence-Key", UUID.randomUUID().toString())
                    .header("Authorization", "Basic " + encodeBasicAuth(yooKassaConfig.getShopId(), yooKassaConfig.getApiKey()))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                log.error("YooKassa API error: status={}, body={}", response.statusCode(), response.body());
                throw new PaymentException("Ошибка при создании платежа: " + response.body());
            }

            String body = response.body();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            String confirmationUrl = node.path("confirmation").path("confirmation_url").asText();

            log.info("Payment created successfully, confirmation URL: {}", confirmationUrl);
            return confirmationUrl;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating payment", e);
            throw new PaymentException("Не удалось создать платеж: " + e.getMessage(), e);
        }
    }

    private String encodeBasicAuth(String shopId, String apiKey) {
        String credentials = shopId + ":" + apiKey;
        return java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}

