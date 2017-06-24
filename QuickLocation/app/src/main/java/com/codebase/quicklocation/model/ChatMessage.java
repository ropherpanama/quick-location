package com.codebase.quicklocation.model;

/**
 * Created by AUrriola on 6/20/17.
 */

public class ChatMessage {
    private String message;
    private String userMessage;
    private long timeOfMessage;

    public ChatMessage() {
    }

    public ChatMessage(String message, String userMessage, long timeOfMessage) {
        this.message = message;
        this.userMessage = userMessage;
        this.timeOfMessage = timeOfMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public long getTimeOfMessage() {
        return timeOfMessage;
    }

    public void setTimeOfMessage(long timeOfMessage) {
        this.timeOfMessage = timeOfMessage;
    }
}
