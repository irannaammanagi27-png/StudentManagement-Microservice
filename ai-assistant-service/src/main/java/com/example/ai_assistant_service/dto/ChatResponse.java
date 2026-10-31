package com.example.ai_assistant_service.dto;


public class ChatResponse {

    private String reply;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        return "ChatResponse [reply=" + reply + "]";
    }
}