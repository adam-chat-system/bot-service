package com.chatapp.botservice.DTO;

public class MessageRequestDTO {

    private String content;
    private String sender;

    public MessageRequestDTO() {
    }

    public MessageRequestDTO(String content, String sender) {
        this.content = content;
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public String getSender() {
        return sender;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}