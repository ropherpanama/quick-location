package com.codebase.quicklocation.firebasedb;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fgcanga on 6/20/17.
 */

public class ChatMessage {
    private String message;
    private String userMessage;
    private long timeOfMessage;
    private TypeGroup users;
    public ChatMessage() {
    }

    public ChatMessage(String message, String userMessage, long timeOfMessage) {
        this.message = message;
        this.userMessage = userMessage;
        this.timeOfMessage = timeOfMessage;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("message",message);
        result.put("userMessage",userMessage);
        result.put("timeOfMessage",timeOfMessage);
        result.put("users",users);
        return result;
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
