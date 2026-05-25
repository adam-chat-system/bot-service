package com.chatapp.botservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue messageQueue() {
        return new Queue("message-queue");
    }

    @Bean
    public TopicExchange messageExchange() {
        return new TopicExchange("message-exchange");
    }

    @Bean
    public Binding binding(Queue messageQueue, TopicExchange messageExchange) {
        return BindingBuilder
                .bind(messageQueue)
                .to(messageExchange)
                .with("message.published");
    }
}