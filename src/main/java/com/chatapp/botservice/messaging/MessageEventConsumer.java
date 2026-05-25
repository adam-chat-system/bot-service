package com.chatapp.botservice.messaging;

import com.chatapp.botservice.DTO.MessageEventDTO;
import com.chatapp.botservice.DTO.MessageRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class MessageEventConsumer {

    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(MessageEventConsumer.class);
    private final WebClient webClient;

    public MessageEventConsumer(
            ObjectMapper objectMapper,
            @Value("${message.service.url}") String messageServiceUrl
    ) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(messageServiceUrl)
                .build();
    }

    @RabbitListener(queues = "message-queue")
    public void handleMessage(String eventJson) {

        try {
            // Parse JSON → Java object
            MessageEventDTO event = objectMapper.readValue(eventJson, MessageEventDTO.class);

            // Stop loop
            if ("bot".equalsIgnoreCase(event.getSender())) {
                return;
            }

            log.info("Bot received event from sender={}", event.getSender());
            String content = event.getContent();

            // simple bot logic
            if (content != null && content.toLowerCase().contains("hello")) {

                String reply = "Hi! Im a bot :D";

                log.info("Bot replying to sender={}", event.getSender());

                MessageRequestDTO request = new MessageRequestDTO(reply, "bot");

                webClient.post()
                        .uri("/messages")
                        .bodyValue(request)
                        .retrieve()
                        .onStatus(
                                status -> status.isError(),
                                response -> response.bodyToMono(String.class)
                                        .doOnNext(body -> log.error("Error response from message-service: {}", body))
                                        .then(Mono.error(new RuntimeException("Request failed")))
                        )
                        .toBodilessEntity()
                        .timeout(Duration.ofSeconds(5))
                        .block();
            }

        } catch (Exception e) {
            log.error("Failed to process message event", e);

            throw new RuntimeException("Failed to process message event", e);
        }
    }
}