package com.chatapp.botservice.messaging;

import com.chatapp.botservice.DTO.MessageEventDTO;
import com.chatapp.botservice.DTO.MessageRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MessageEventConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient = WebClient.create("http://localhost:8083"); // message-service

    @RabbitListener(queues = "message-queue")
    public void handleMessage(String eventJson) {

        try {
            // Parse JSON → Java object
            MessageEventDTO event = objectMapper.readValue(eventJson, MessageEventDTO.class);

            if ("bot".equalsIgnoreCase(event.getSender())) {
                return;
            }

            System.out.println("🤖 Bot received event: " + event.getSender() + ": " + event.getContent());


            // easy bot reactions
            if (event.getContent().toLowerCase().contains("hello")) {

                String reply = "Hi! Im a bot :D";

                System.out.println("Bot replying: " + reply);

                MessageRequestDTO request = new MessageRequestDTO(reply, "bot");

                webClient.post()
                        .uri("/messages")
                        .header("Content-Type", "application/json")
                        .bodyValue(request)
                        .retrieve()
                        .onStatus(
                                status -> status.isError(),
                                response -> response.bodyToMono(String.class)
                                        .doOnNext(body -> System.out.println("ERROR BODY: " + body))
                                        .then(Mono.error(new RuntimeException("Request failed")))
                        )
                        .toBodilessEntity()
                        .block();
            }

        } catch (Exception e) {
            System.err.println("Failed to process message: " + e.getMessage());
        }
    }
}