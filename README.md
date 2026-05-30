# Bot Service

Automated chatbot service for the chat-system.

## Technologies

* Java 21
* Spring Boot
* RabbitMQ
* WebClient

## Features

* Consumes message events from RabbitMQ
* Generates automated chatbot responses
* Sends bot replies to the Message Service
* Prevents bot message loops

## Run

```bash
mvn clean package

docker build -t bot-service .
```

## Configuration

```properties
message.service.url=http://message-service:8083

spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
```

## Message Flow

```text
Message Service
      ↓
   RabbitMQ
      ↓
  Bot Service
      ↓
Message Service
```
