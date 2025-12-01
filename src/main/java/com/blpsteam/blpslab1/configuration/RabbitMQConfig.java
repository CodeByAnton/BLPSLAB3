package com.blpsteam.blpslab1.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue stompReviewsQueue() {
        return new Queue("stomp.reviews", true);
    }

    @Bean
    public TopicExchange stompTopicExchange() {
        return new TopicExchange("amq.topic"); // Используем стандартный exchange
    }

    @Bean
    public Binding stompReviewsBinding() {
        return BindingBuilder.bind(stompReviewsQueue())
                .to(stompTopicExchange())
                .with("queue.reviews"); // routing key должен совпадать с STOMP destination
    }
}