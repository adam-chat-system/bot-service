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
            MessageEventDTO event = objectMapper.readValue(eventJson, MessageEventDTO.class);

            // Stop loop
            if ("bot".equalsIgnoreCase(event.getSender())) {
                return;
            }

            log.info("Bot received event from sender={}", event.getSender());

            String content = event.getContent();
            String message = content == null ? "" : content.trim().toLowerCase();

            String reply = null;

            if (message.equals("hello") || message.equals("hi")) {

                reply = """
                    Hello! How can I help you today?

                    1. Delivery
                    2. Contact Support
                    3. Opening Hours
                    4. Return Policy

                    Please enter a number.
                    """;
            }
            else if (message.equals("1")) {

                reply = """
                    Delivery normally takes 2-4 business days.

                    Was this information helpful?

                    Yes / No
                    """;
            }
            else if (message.equals("2")) {

                reply = """
                    You can contact support at:

                    support@chatapp.com

                    Was this information helpful?

                    Yes / No
                    """;
            }
            else if (message.equals("3")) {

                reply = """
                    Our opening hours are:

                    Monday-Friday
                    08:00 - 17:00

                    Was this information helpful?

                    Yes / No
                    """;
            }
            else if (message.equals("4")) {

                reply = """
                    Returns are accepted within 30 days.

                    Was this information helpful?

                    Yes / No
                    """;
            }
            else if (message.equals("yes")) {

                reply = """
                    Great! I'm glad I could help.

                    If you need anything else,
                    type hello to start again.
                    """;
            }
            else if (message.equals("no")) {

                reply = """
                    I'm sorry to hear that.

                    Please contact support@chatapp.com
                    for further assistance.

                    Type hello to start again.
                    """;
            }

            if (reply != null) {

                log.info("Bot replying to sender={}", event.getSender());

                MessageRequestDTO request =
                        new MessageRequestDTO(reply, "bot");

                webClient.post()
                        .uri("/messages")
                        .bodyValue(request)
                        .retrieve()
                        .onStatus(
                                status -> status.isError(),
                                response -> response.bodyToMono(String.class)
                                        .doOnNext(body ->
                                                log.error("Error response from message-service: {}", body))
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