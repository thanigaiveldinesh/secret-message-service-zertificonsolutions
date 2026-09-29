package com.crypto.secret.message.exception;

public class MessageException extends RuntimeException {
    private final int status;

    public MessageException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
