package com.skinsense.exception;

public class GeminiException extends RuntimeException {

    private final String userMessage;

    public GeminiException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }

    public GeminiException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
